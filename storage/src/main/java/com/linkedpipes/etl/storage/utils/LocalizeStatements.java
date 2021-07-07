package com.linkedpipes.etl.storage.utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LocalizeStatements {

    protected LocalizeStatements() {
    }

    public static Statements allWithType(
            Collection<Statement> statements, Resource target) {
        return withType(statements, (type) -> true, target.stringValue());
    }

    public static Statements allWithType(
            Collection<Statement> statements, String target) {
        return withType(statements, (type) -> true, target);
    }

    /**
     * Localize all statements whose resources are of given type.
     */
    public static Statements withType(
            Collection<Statement> statements,
            Predicate<Resource> type,
            String target) {
        if (statements == null) {
            return null;
        }
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Map<Resource, Resource> mapping = new HashMap<>();
        statements.stream()
                .filter(st -> RDF.TYPE.equals(st.getPredicate()))
                .filter(st -> st.getObject().isIRI())
                .filter(st -> type.test((IRI) st.getObject()))
                .map(Statement::getSubject)
                .forEach(resource -> mapping.put(
                        resource,
                        valueFactory.createIRI(target + "/" + mapping.size())));
        List<Statement> result = statements.stream()
                .map((st) -> valueFactory.createStatement(
                        mapping.getOrDefault(st.getSubject(), st.getSubject()),
                        st.getPredicate(),
                        updateValue(mapping, st.getObject()),
                        mapping.getOrDefault(st.getContext(), st.getContext())
                )).collect(Collectors.toList());
        return Statements.wrap(result);
    }

    protected static Value updateValue(
            Map<Resource, Resource> mapping, Value value) {
        if (value.isResource()) {
            return mapping.getOrDefault((Resource) value, (Resource) value);
        }
        return value;
    }

    /**
     * For all given named resources, and graphs, replace source
     * prefix with target prefix.
     */
    public static Statements withPrefix(
            Collection<Statement> statements,
            Resource source, String target) {
        // Collect mapping of resources.
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        String prefix = source.stringValue();
        List<Statement> result = statements.stream()
                .map((st) -> valueFactory.createStatement(
                        updateResource(prefix, target, st.getSubject()),
                        st.getPredicate(),
                        updateValue(prefix, target, st.getObject()),
                        updateResource(prefix, target, st.getContext())
                )).collect(Collectors.toList());
        return Statements.wrap(result);
    }

    protected static Resource updateResource(
            String prefix, String targetIri, Resource resource) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        if (resource == null) {
            return null;
        }
        if (!resource.isIRI()) {
            return resource;
        }
        String iri = resource.stringValue().replace(prefix, targetIri);
        return valueFactory.createIRI(iri);
    }

    protected static Value updateValue(
            String prefix, String targetIri, Value value) {
        if (value instanceof Resource) {
            return updateResource(prefix, targetIri, (Resource) value);
        } else {
            return value;
        }
    }

}
