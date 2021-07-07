package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PluginDefinitionAdapter {

    protected static final String TYPE =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static PluginDefinition create(Collection<Statement> definition) {
        Statements statements = Statements.wrap(definition);
        Resource resource = statements.selectSubjectOrDefaultIri(
                RDF.TYPE, LP_PIPELINE.JAR_TEMPLATE, null);
        if (resource == null) {
            return null;
        }
        PluginDefinition result = new PluginDefinition();
        result.resource = resource;
        for (Statement statement :
                statements.select(resource, (IRI) null, null)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case SKOS.PREF_LABEL:
                    result.prefLabel = value;
                    break;
                case LP_PIPELINE.HAS_COLOR:
                    result.color = value;
                    break;
                case LP_PIPELINE.HAS_COMPONENT_TYPE:
                    if (value instanceof IRI) {
                        result.type = (IRI) value;
                    }
                    break;
                case LP_PIPELINE.HAS_SUPPORT_CONTROL:
                    result.supportControl = value;
                    break;
                case LP_PIPELINE.HAS_KEYWORD:
                    result.tags.add(value);
                    break;
                case LP_PIPELINE.HAS_INFO_LINK:
                    if (value instanceof IRI) {
                        result.infoLink = (IRI) value;
                    }
                    break;
                case LP_PIPELINE.HAS_PORT:
                    if (value instanceof Resource) {
                        result.ports.add(loadPort(statements, (Resource) value));
                    }
                    break;
                case LP_PIPELINE.HAS_REQUIREMENT:
                    result.requirement.add(value);
                    break;
                case LP_PIPELINE.HAS_JAR_URL:
                    if (value instanceof  Resource) {
                        result.jarResource = (Resource) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    protected static PluginDefinition.Port loadPort(
            Statements statements, Resource resource) {
        PluginDefinition.Port result = new PluginDefinition.Port();
        for (Statement statement :
                statements.select(resource, (IRI) null, null)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_BINDING:
                    result.binding = value;
                    break;
                case SKOS.PREF_LABEL:
                    result.prefLabel = value;
                    break;
                case TYPE:
                    if (value instanceof IRI) {
                        result.types.add((IRI) value);
                    }
                    break;
                case LP_PIPELINE.HAS_REQUIREMENT:
                    result.requirement.add(value);
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public static Statements asStatements(PluginDefinition definition) {
        Statements result = Statements.arrayList();
        Resource resource = definition.resource;
        result.addIri(resource, RDF.TYPE, LP_PIPELINE.JAR_TEMPLATE);
        result.add(resource, SKOS.PREF_LABEL, definition.prefLabel);
        result.add(resource, LP_PIPELINE.HAS_COLOR, definition.color);
        result.add(resource, LP_PIPELINE.HAS_COMPONENT_TYPE, definition.type);
        result.add(
                resource, LP_PIPELINE.HAS_SUPPORT_CONTROL,
                definition.supportControl);
        for (Value keyword : definition.tags) {
            result.add(resource, LP_PIPELINE.HAS_KEYWORD, keyword);
        }
        result.add(resource, LP_PIPELINE.HAS_INFO_LINK, definition.infoLink);
        result.add(
                resource, LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                definition.configurationGraph);
        result.add(
                resource, LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                definition.configurationDescriptionGraph);
        result.add(
                resource, LP_PIPELINE.HAS_JAR_URL, definition.jarResource);
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Set<String> dialogs = new HashSet<>();
        // We store dialog as references to pair of html and js files.
        for (String dialog : definition.dialogs) {
            // Cut only part of the name.
            dialog = dialog.substring(0, dialog.indexOf("/"));
            dialogs.add(dialog);
        }
        for (String dialog : dialogs) {
            Resource dialogResource = valueFactory.createIRI(
                    definition.resource.stringValue() + "/dialog/" + dialog);
            result.add(resource, LP_PIPELINE.HAS_DIALOG, dialogResource);
            result.addIri(dialogResource, RDF.TYPE, LP_PIPELINE.DIALOG);
            result.addString(dialogResource, LP_PIPELINE.HAS_NAME, dialog);
        }
        for (PluginDefinition.Port port : definition.ports) {
            Resource portResource = valueFactory.createIRI(
                    definition.resource.stringValue() + "/port/" +
                            port.binding.stringValue());
            result.add(resource, LP_PIPELINE.HAS_PORT, portResource);
            for (Resource type : port.types) {
                result.add(portResource, RDF.TYPE, type);
            }
            result.add(portResource, SKOS.PREF_LABEL, port.prefLabel);
            result.add(portResource, LP_PIPELINE.HAS_BINDING, port.binding);
            for (Value requirement : definition.requirement) {
                result.add(
                        portResource, LP_PIPELINE.HAS_REQUIREMENT, requirement);
            }
        }
        for (Value requirement : definition.requirement) {
            result.add(resource, LP_PIPELINE.HAS_REQUIREMENT, requirement);
        }
        return result;
    }

}
