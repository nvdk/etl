package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateV5 {

    /**
     * This is the first version where version is stored as a part of a
     * template.
     */
    public static Integer loadVersion(
            Resource resource, Statements statements) {
        List<Literal> parents = statements.select(
                resource, "http://etl.linkedpipes.com/ontology/version", null)
                .objects().stream()
                .filter(Value::isLiteral)
                .map(value -> (Literal) value)
                .collect(Collectors.toList());
        if (parents.size() == 1) {
            return parents.get(0).intValue();
        }
        return null;
    }

    public static Resource loadKnownAs(
            Resource resource, Statements statements) {
        List<Resource> knownAs = statements.select(
                resource, "http://www.w3.org/2002/07/owl#sameAs", null)
                .objects().stream()
                .filter(Value::isResource)
                .map(item -> (Resource)item)
                .collect(Collectors.toList());
        if (knownAs.size() == 1) {
            return knownAs.get(0);
        }
        return null;
    }

}
