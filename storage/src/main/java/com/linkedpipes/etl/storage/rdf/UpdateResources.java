package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use to update configuration from one domain to another.
 */
public class UpdateResources {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final String newPrefix;

    private final Map<Resource, Resource> mapping = new HashMap<>();

    protected UpdateResources(String newPrefix) {
        this.newPrefix = newPrefix;
    }

    public static Statements apply(String prefix, Statements statements) {
        return (new UpdateResources(prefix)).update(statements);
    }

    protected Statements update(Statements statements) {
        buildMapping(statements);
        List<Statement> result = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            result.add(valueFactory.createStatement(
                    updateResource(statement.getSubject()),
                    statement.getPredicate(),
                    updateValue(statement.getObject()),
                    updateResource(statement.getContext())
            ));
        }
        mapping.clear();
        return Statements.wrap(result);
    }

    protected void buildMapping(Statements statements) {
        mapping.clear();
        int counter = 0;
        for (Statement statement : statements) {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                mapping.put(statement.getSubject(), valueFactory.createIRI(
                        newPrefix + String.format("%03d", counter++)));
            }
        }
    }

    protected Resource updateResource(Resource resource) {
        if (resource == null || !resource.isResource()) {
            return resource;
        }
        return mapping.getOrDefault(resource, resource);
    }

    protected Value updateValue(Value value) {
        if (value instanceof Resource resource) {
            return updateResource(resource);
        } else {
            return value;
        }
    }

}
