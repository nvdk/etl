package com.linkedpipes.etl.storage.template.plugin.adapter.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescriptionAdapter;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.rdf.UpdateResources;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RdfToPluginTemplate {

    private static final String TYPE =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private static final Resource JAR_TEMPLATE =
            SimpleValueFactory.getInstance().createIRI(LP_PIPELINE.JAR_TEMPLATE);

    public static List<PluginTemplate> asPluginTemplates(
            StatementsSelector statements) {
        Collection<Resource> resources =
                statements.selectSubjects(RDF.TYPE, JAR_TEMPLATE).subjects();
        List<PluginTemplate> result = new ArrayList<>();
        for (Resource resource : resources) {
            result.add(loadPluginTemplate(statements, resource));
        }

        return result;
    }

    private static PluginTemplate loadPluginTemplate(
            StatementsSelector statements, Resource pluginResource) {
        Value prefLabel = null, color = null;
        IRI type = null, infoLink = null;
        Literal supportControl = null;
        Resource jarResource = null;
        Resource configurationGraph =
                PluginTemplate.defaultConfigurationGraph(pluginResource);
        Resource descriptionGraph =
                PluginTemplate.defaultConfigurationDescriptionGraph(
                        pluginResource);
        List<Value> tags = new ArrayList<>();
        List<PluginTemplate.Port> ports = new ArrayList<>();
        List<IRI> requirement = new ArrayList<>();
        List<String> dialogs = new ArrayList<>();

        for (Statement statement : statements.withResource(pluginResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case SKOS.PREF_LABEL:
                    prefLabel = value;
                    break;
                case LP_PIPELINE.HAS_COLOR:
                    color = value;
                    break;
                case LP_PIPELINE.HAS_COMPONENT_TYPE:
                    if (value instanceof IRI iri) {
                        type = iri;
                    }
                    break;
                case LP_PIPELINE.HAS_SUPPORT_CONTROL:
                    if (value instanceof Literal literal) {
                        supportControl = literal;
                    }
                    break;
                case LP_PIPELINE.HAS_KEYWORD:
                    tags.add(value);
                    break;
                case LP_PIPELINE.HAS_INFO_LINK:
                    if (value instanceof IRI iri) {
                        infoLink = iri;
                    }
                    break;
                case LP_PIPELINE.HAS_PORT:
                    if (value instanceof Resource resource) {
                        ports.add(loadPort(statements, resource));
                    }
                    break;
                case LP_PIPELINE.HAS_REQUIREMENT:
                    if (value instanceof IRI iri) {
                        requirement.add(iri);
                    }
                    break;
                case LP_PIPELINE.HAS_JAR_URL:
                    if (value instanceof Resource resource) {
                        jarResource = resource;
                    }
                    break;
                case LP_PIPELINE.HAS_DIALOG:
                    if (value instanceof Resource resource) {
                        String dialogName = loadDialog(statements, resource);
                        if (dialogName != null) {
                            dialogs.add(dialogName);
                        }
                    }
                default:
                    break;
            }
        }

        Statements configuration = statements
                .selectByGraph(configurationGraph).withoutGraph();

        ConfigurationDescription description = ConfigurationDescriptionAdapter
                .asConfigurationDescription(
                        statements.selectByGraph(descriptionGraph));

        return new PluginTemplate(
                pluginResource,
                prefLabel,
                color,
                type,
                supportControl,
                tags,
                infoLink,
                dialogs,
                ports,
                jarResource,
                requirement,
                configuration,
                configurationGraph,
                description,
                descriptionGraph
        );
    }

    private static PluginTemplate.Port loadPort(
            StatementsSelector statements, Resource resource) {
        Value binding = null, prefLabel = null;
        List<IRI> types = new ArrayList<>();
        List<IRI> requirement = new ArrayList<>();
        for (Statement statement : statements.withResource(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_BINDING:
                    binding = value;
                    break;
                case SKOS.PREF_LABEL:
                    prefLabel = value;
                    break;
                case TYPE:
                    if (value instanceof IRI iri) {
                        types.add(iri);
                    }
                    break;
                case LP_PIPELINE.HAS_REQUIREMENT:
                    if (value instanceof IRI iri) {
                        requirement.add(iri);
                    }
                    break;
                default:
                    break;
            }
        }
        return new PluginTemplate.Port(binding, prefLabel, types, requirement);
    }

    private static String loadDialog(
            StatementsSelector statements, Resource resource) {
        for (Statement statement : statements.withResource(resource)) {
            Value value = statement.getObject();
            String predicate = statement.getPredicate().stringValue();
            if (LP_PIPELINE.HAS_NAME.equals(predicate)) {
                return value.stringValue();
            }
        }
        return null;
    }

    /**
     * Design to load data from a plugin definition file.
     */
    public static PluginTemplate asPluginTemplate(
            StatementsSelector definitionStatements,
            Statements configurationStatements,
            Statements configurationDescriptionStatements)
            throws StorageException {
        Collection<Resource> resources =
                definitionStatements.selectSubjects(RDF.TYPE, JAR_TEMPLATE)
                        .subjects();
        if (resources.size() != 1) {
            throw new StorageException("Missing template resource.");
        }
        Resource resource = resources.iterator().next();
        PluginTemplate template = loadPluginTemplate(
                definitionStatements, resource);

        Statements configuration = UpdateResources.apply(
                template.configurationGraph().stringValue() + "/",
                configurationStatements
        ).withoutGraph();

        ConfigurationDescription description =
                ConfigurationDescriptionAdapter.asConfigurationDescription(
                        configurationDescriptionStatements);
        if (description != null) {
            description.resource = template.configurationDescriptionGraph();
        }

        return new PluginTemplate(
                template.resource(), template.prefLabel(), template.color(),
                template.type(), template.supportControl(), template.tags(),
                template.infoLink(), template.dialogs(), template.ports(),
                template.jarResource(), template.requirement(),
                configuration,
                template.configurationGraph(),
                description,
                template.configurationDescriptionGraph());
    }

}
