package com.linkedpipes.etl.storage.template.reference.adapter.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class ReferenceTemplateToRdf {

    public static Statements definitionAsRdf(
            ReferenceTemplate definition) {
        StatementsBuilder result = Statements.arrayList().builder();
        Resource resource = definition.resource();
        result.addIri(resource, RDF.TYPE, LP_PIPELINE.REFERENCE_TEMPLATE);
        result.add(resource, LP_PIPELINE.HAS_TEMPLATE, definition.template());
        if (definition.prefLabel() != null) {
            result.add(resource, SKOS.PREF_LABEL, definition.prefLabel());
        }
        if (definition.description() != null) {
            result.add(
                    resource, LP_PIPELINE.HAS_DESCRIPTION,
                    definition.description());
        }
        if (definition.note() != null) {
            result.add(resource, LP_PIPELINE.HAS_NOTE, definition.note());
            result.add(resource, LP_PIPELINE.HAS_COLOR, definition.color());
        }
        for (Value keyword : definition.tags()) {
            result.add(resource, LP_PIPELINE.HAS_KEYWORD, keyword);
        }
        if (definition.knownAs() != null) {
            result.add(resource, LP_PIPELINE.HAS_KNOWN_AS, definition.knownAs());
        }
        result.add(resource, LP_PIPELINE.HAS_PLUGIN_TEMPLATE,
                definition.pluginTemplate());
        result.add(resource, LP.HAS_VERSION, definition.version());
        result.add(
                resource, LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                definition.configurationGraph());
        return result.withGraph(definition.resource());
    }

    public static Statements configurationAsRdf(ReferenceTemplate definition) {
        return definition.configuration()
                .withGraph(definition.configurationGraph());
    }

}
