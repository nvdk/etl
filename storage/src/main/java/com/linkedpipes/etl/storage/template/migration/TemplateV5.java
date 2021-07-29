package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateV5 {

    public static Resource loadParent(
            Resource resource, Statements statements) {
        return TemplateV0.loadParent(resource, statements);
    }

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

    public static Resource loadConfiguration(
            Resource resource, Statements statements) {
        return TemplateV0.loadConfiguration(resource, statements);
    }

}
