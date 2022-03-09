package com.linkedpipes.etl.storage.template.reference.adapter.rdf;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdfToReferenceTemplate {

    public static List<ReferenceTemplate> asReferenceTemplates(
            StatementsSelector statements) {
        Collection<Resource> resources = statements.selectSubjectsWithType(
                LP_PIPELINE.REFERENCE_TEMPLATE).subjects();
        List<ReferenceTemplate> result = new ArrayList<>();
        for (Resource resource : resources) {
            result.add(asReferenceTemplate(statements, null, resource));
        }
        return loadMapping(result, statements);
    }

    public static ReferenceTemplate asReferenceTemplate(
            StatementsSelector statements, Statements configuration)
            throws StorageException {
        Collection<Resource> resources = statements.selectSubjectsWithType(
                LP_PIPELINE.REFERENCE_TEMPLATE).subjects();
        if (resources.size() != 1) {
            throw new StorageException(
                    "Invalid number ({}) of references detected.",
                    resources.size());
        }
        Resource resource = resources.iterator().next();
        return asReferenceTemplate(statements, configuration, resource);
    }

    private static ReferenceTemplate asReferenceTemplate(
            StatementsSelector statements, Statements configuration,
            Resource templateResource) {
        IRI template = null, pluginTemplate = null, knownAs = null;
        Resource configurationGraph = null;
        Literal prefLabel = null, description = null, note = null,
                version = null;
        Value color = null;
        List<Literal> tags = new ArrayList<>();

        for (Statement statement : statements.withResource(templateResource)) {
            Value value = statement.getObject();
            String predicate = statement.getPredicate().stringValue();
            switch (predicate) {
                case LP_PIPELINE.HAS_TEMPLATE:
                    if (value instanceof IRI iri) {
                        template = iri;
                    }
                    break;
                case SKOS.PREF_LABEL:
                    if (value instanceof Literal literal) {
                        prefLabel = literal;
                    }
                    break;
                case LP_PIPELINE.HAS_DESCRIPTION:
                    if (value instanceof Literal literal) {
                        description = literal;
                    }
                    break;
                case LP_PIPELINE.HAS_NOTE:
                    if (value instanceof Literal literal) {
                        note = literal;
                    }
                    break;
                case LP_PIPELINE.HAS_COLOR:
                    color = value;
                    break;
                case LP_PIPELINE.HAS_KEYWORD:
                    if (value instanceof Literal literal) {
                        tags.add(literal);
                    }
                    break;
                // Added in version 3
                case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                    if (value instanceof Resource resource) {
                        configurationGraph = resource;
                    }
                    break;
                // Added in version 5
                case LP_PIPELINE.HAS_PLUGIN_TEMPLATE:
                    if (value instanceof IRI iri) {
                        pluginTemplate = iri;
                    }
                    break;
                case LP.HAS_VERSION:
                    if (value instanceof Literal literal) {
                        version = literal;
                    }
                    break;
                case LP_PIPELINE.HAS_KNOWN_AS:
                    if (value instanceof IRI iri) {
                        knownAs = iri;
                    }
                    break;
            }
        }
        return new ReferenceTemplate(
                templateResource, template, prefLabel, description, note,
                color, tags, knownAs, pluginTemplate, version,
                loadConfiguration(
                        statements, configuration,
                        templateResource, configurationGraph),
                configurationGraph);
    }

    private static Statements loadConfiguration(
            StatementsSelector statements,
            Statements configuration,
            Resource templateResource,
            Resource configurationGraph) {
        if (configurationGraph == null) {
            // We try to guess the configuration graph. The graph was not
            // present in some versions, but there were a convention
            // in naming the configuration graph.
            configurationGraph = ReferenceTemplate
                    .defaultConfigurationGraph(templateResource);
        }
        if (configuration == null || configuration.isEmpty()) {
            // Try to load it from the original data.
            return statements.selectByGraph(configurationGraph).withoutGraph();
        } else {
            return configuration.withoutGraph();
        }
    }

    /**
     * Mapping information used to be, prior to version 5, in an extra
     * graph.
     */
    private static List<ReferenceTemplate> loadMapping(
            List<ReferenceTemplate> templates,
            Collection<Statement> statements) {
        Map<Resource, Resource> localToOriginal = new HashMap<>();
        Resource graph = SimpleValueFactory.getInstance()
                .createIRI(LP.MAPPING_GRAPH);
        statements.stream()
                .filter((s) -> Objects.equal(s.getContext(), graph))
                .filter((s) -> s.getPredicate().equals(OWL.SAMEAS))
                .forEach((s) -> {
                    Resource original = s.getSubject();
                    Value local = s.getObject();
                    if (local instanceof Resource) {
                        localToOriginal.put((Resource) local, original);
                    }
                });
        List<ReferenceTemplate> result = new ArrayList<>();
        for (ReferenceTemplate template : templates) {
            Resource knownAs = localToOriginal.get(template.resource());
            if (template.knownAs() == null && knownAs != null) {
                result.add(new ReferenceTemplate(
                        template.resource(), template.template(),
                        template.prefLabel(), template.description(),
                        template.note(), template.color(),
                        template.tags(), knownAs, template.pluginTemplate(),
                        template.version(), template.configuration(),
                        template.configurationGraph()));
            } else {
                result.add(template);
            }
        }
        return result;
    }

}
