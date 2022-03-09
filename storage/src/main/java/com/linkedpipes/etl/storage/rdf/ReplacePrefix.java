package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReplacePrefix {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final String oldPrefix;

    private final String newPrefix;

    public ReplacePrefix(String oldPrefix, String newPrefix) {
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
    }

    /**
     * Replace all based on a prefix.
     */
    public Collection<Statement> replace(Collection<Statement> statements) {
        List<Statement> result = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            result.add(valueFactory.createStatement(
                    updateResource(statement.getSubject()),
                    statement.getPredicate(),
                    updateValue(statement.getObject()),
                    updateResource(statement.getContext())
            ));
        }
        return result;
    }

    protected Resource updateResource(Resource resource) {
        if (resource == null || !resource.isIRI()) {
            return resource;
        }
        String iri = resource.stringValue().replace(oldPrefix, newPrefix);
        return valueFactory.createIRI(iri);
    }

    protected Value updateValue(Value value) {
        if (value instanceof Resource) {
            return updateResource((Resource) value);
        } else {
            return value;
        }
    }

}
