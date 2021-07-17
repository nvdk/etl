package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;

public class ReferenceDefinitionAdapter {

    public static ReferenceDefinition create(
            Collection<Statement> definition) {
        Statements statements = Statements.wrap(definition);
        Resource resource = statements.selectSubjectOrDefaultIri(
                RDF.TYPE, LP_PIPELINE.REFERENCE_TEMPLATE, null);
        if (resource == null) {
            return null;
        }
        ReferenceDefinition result = new ReferenceDefinition();
        result.resource = resource;
        for (Statement statement :
                statements.select(resource, (IRI) null, null)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_TEMPLATE:
                    if (value instanceof IRI) {
                        result.template = (IRI) value;
                    }
                case SKOS.PREF_LABEL:
                    result.prefLabel = value;
                    break;
                case LP_PIPELINE.HAS_DESCRIPTION:
                    result.description = value;
                    break;
                case LP_PIPELINE.HAS_NOTE:
                    result.note = value;
                    break;
                case LP_PIPELINE.HAS_COLOR:
                    result.color = value;
                    break;
                case LP_PIPELINE.HAS_KEYWORD:
                    result.tags.add(value);
                    break;
                case LP_PIPELINE.HAS_KNOWN_AS:
                    if (value instanceof IRI) {
                        result.knownAs = (IRI) value;
                    }
                case LP_PIPELINE.HAS_ROOT:
                    if (value instanceof IRI) {
                        result.root = (IRI) value;
                    }
                case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                    if (value instanceof IRI) {
                        result.configurationGraph = (IRI) value;
                    }
            }
        }
        return result;
    }

    public static Statements asStatements(ReferenceDefinition definition) {
        Statements result = Statements.arrayList();
        result.withGraph(definition.resource);
        Resource resource = definition.resource;
        result.addIri(resource, RDF.TYPE, LP_PIPELINE.REFERENCE_TEMPLATE);
        result.add(resource, LP_PIPELINE.HAS_TEMPLATE, definition.template);
        if (definition.prefLabel != null) {
            result.add(resource, SKOS.PREF_LABEL, definition.prefLabel);
        }
        if (definition.description != null) {
            result.add(
                    resource, LP_PIPELINE.HAS_DESCRIPTION,
                    definition.description);
        }
        if (definition.note != null) {
            result.add(resource, LP_PIPELINE.HAS_NOTE, definition.note);
            result.add(resource, LP_PIPELINE.HAS_COLOR, definition.color);
        }
        for (Value keyword : definition.tags) {
            result.add(resource, LP_PIPELINE.HAS_KEYWORD, keyword);
        }
        if (definition.knownAs != null) {
            result.add(resource, LP_PIPELINE.HAS_KNOWN_AS, definition.knownAs);
        }
        result.add(resource, LP_PIPELINE.HAS_ROOT, definition.root);
        result.add(
                resource, LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                definition.configurationGraph);
        return result;
    }

}
