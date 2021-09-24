package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.stream.Collectors;

class TemplateV0 {

    /**
     * There were no changes between 0 and 1 from the perspective of
     * templates.
     */
    public ReferenceContainer migrateToV1(ReferenceContainer template) {
        return template;
    }

    public static Resource loadParent(
            Resource resource, Statements statements) {
        List<Resource> parents = statements.select(
                resource, "http://linkedpipes.com/ontology/template", null)
                .objects().stream()
                .filter(Value::isIRI)
                .map(value -> (IRI) value)
                .collect(Collectors.toList());
        if (parents.size() == 1) {
            return parents.get(0);
        }
        return null;
    }

    public static Resource loadConfiguration(
            Resource resource, Statements statements) {
        // http://linkedpipes.com/ontology/configurationGraph
        List<Resource> parents = statements.select(
                resource, "http://linkedpipes.com/ontology/configurationGraph", null)
                .objects().stream()
                .filter(Value::isIRI)
                .map(value -> (IRI) value)
                .collect(Collectors.toList());
        if (parents.size() == 1) {
            return parents.get(0);
        }
        return null;
    }

}
