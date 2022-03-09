package com.linkedpipes.etl.storage.template.plugin.adapter.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescriptionAdapter;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.rdf.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class PluginTemplateToRdf {

    public Statements definitionAsRdf(PluginTemplate definition) {
        StatementsBuilder result = Statements.arrayList().builder();
        Resource resource = definition.resource();
        result.setDefaultGraph(resource);
        result.addIri(resource, RDF.TYPE, LP_PIPELINE.JAR_TEMPLATE);
        result.add(resource, SKOS.PREF_LABEL, definition.prefLabel());
        result.add(resource, LP_PIPELINE.HAS_COLOR, definition.color());
        result.add(resource, LP_PIPELINE.HAS_COMPONENT_TYPE, definition.type());
        result.add(
                resource, LP_PIPELINE.HAS_SUPPORT_CONTROL,
                definition.supportControl());
        for (Value keyword : definition.tags()) {
            result.add(resource, LP_PIPELINE.HAS_KEYWORD, keyword);
        }
        result.add(resource, LP_PIPELINE.HAS_INFO_LINK, definition.infoLink());
        result.add(
                resource, LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                definition.configurationGraph());
        result.add(
                resource, LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                definition.configurationDescriptionGraph());
        result.add(
                resource, LP_PIPELINE.HAS_JAR_URL, definition.jarResource());
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (String dialog : definition.dialogs()) {
            Resource dialogResource = valueFactory.createIRI(
                    definition.resource().stringValue() + "/dialog/" + dialog);
            result.add(resource, LP_PIPELINE.HAS_DIALOG, dialogResource);
            result.addIri(dialogResource, RDF.TYPE, LP_PIPELINE.DIALOG);
            result.addString(dialogResource, LP_PIPELINE.HAS_NAME, dialog);
        }
        for (PluginTemplate.Port port : definition.ports()) {
            Resource portResource = valueFactory.createIRI(
                    definition.resource().stringValue() + "/port/" +
                            port.binding().stringValue());
            result.add(resource, LP_PIPELINE.HAS_PORT, portResource);
            for (Resource type : port.types()) {
                result.add(portResource, RDF.TYPE, type);
            }
            result.add(portResource, SKOS.PREF_LABEL, port.prefLabel());
            result.add(portResource, LP_PIPELINE.HAS_BINDING, port.binding());
            for (Value requirement : definition.requirement()) {
                result.add(
                        portResource, LP_PIPELINE.HAS_REQUIREMENT, requirement);
            }
        }
        for (Value requirement : definition.requirement()) {
            result.add(resource, LP_PIPELINE.HAS_REQUIREMENT, requirement);
        }
        return result;
    }

    public Statements configurationAsRdf(PluginTemplate definition) {
        return definition.configuration()
                .withGraph(definition.configurationGraph());
    }

    public Statements configurationDescriptionAsRdf(PluginTemplate definition) {
        return Statements.wrap(ConfigurationDescriptionAdapter.asStatements(
                        definition.configurationDescription()))
                .withGraph(definition.configurationDescriptionGraph());
    }

}
