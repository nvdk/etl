package com.linkedpipes.etl.storage.rdf;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Allow use of selection functions over a collection of statements.
 */
public class StatementsSelector extends Statements {

    private Map<Resource, Statements> resourceIndex = null;

    public StatementsSelector(Collection<Statement> collection) {
        super(collection);
    }

    public Statements selectIri(Resource s, String p, String o) {
        return select(s, valueFactory.createIRI(p), valueFactory.createIRI(o));
    }

    public Statements selectIri(Resource s, IRI p, String o) {
        return select(s, p, valueFactory.createIRI(o));
    }

    public Statements select(Resource s, String p, Value o) {
        return select(s, valueFactory.createIRI(p), o);
    }

    public Statements select(Resource s, IRI p, Value o) {
        Statements result = Statements.arrayList();
        for (Statement statement : collection) {
            if (s != null && !s.equals(statement.getSubject())) {
                continue;
            }
            if (p != null && !p.equals(statement.getPredicate())) {
                continue;
            }
            if (o != null && !o.equals(statement.getObject())) {
                continue;
            }
            result.add(statement);
        }
        return result;
    }

    private static final Logger LOG = LoggerFactory.getLogger(StatementsSelector.class);

    public Statements withResource(Resource s) {
        if (resourceIndex == null) {
            return select(s, (IRI) null, null);
        }
        return resourceIndex.getOrDefault(s, Statements.empty());
    }

    /**
     * Consume memory to speed up {@link #withResource(Resource)}
     * function call.
     */
    public void buildResourceIndex() {
        resourceIndex = new HashMap<>();
        for (Statement statement : collection) {
            resourceIndex.computeIfAbsent(
                    statement.getSubject(),
                    resource -> Statements.arrayList()
            ).add(statement);
        }
    }

    public Statements selectSubjectsWithType(String o) {
        return selectSubjectsWithType(valueFactory.createIRI(o));
    }

    public Statements selectSubjectsWithType(IRI o) {
        return select(null, RDF.TYPE, o);
    }

    public Statements selectSubjects(IRI p, Value o) {
        return select(null, p, o);
    }

    public Resource selectSubjectOrDefaultIri(
            IRI p, String o, Resource defaultValue) {
        return selectSubjectOrDefault(
                p, valueFactory.createIRI(o), defaultValue);
    }

    public Resource selectSubjectOrDefault(
            IRI p, Value o, Resource defaultValue) {
        Collection<Resource> candidates = select(null, p, o).subjects();
        if (candidates.size() != 1) {
            return defaultValue;
        }
        return candidates.iterator().next();
    }

    public <T extends Throwable> Resource selectSubjectOrThrowIri(
            IRI p, String o, Supplier<T> supplier) throws T {
        return selectSubjectOrThrow(p, valueFactory.createIRI(o), supplier);
    }

    public <T extends Throwable> Resource selectSubjectOrThrow(
            IRI p, Value o, Supplier<T> supplier) throws T {
        Collection<Resource> candidates = select(null, p, o).subjects();
        if (candidates.size() != 1) {
            throw supplier.get();
        }
        return candidates.iterator().next();
    }

    public Statements selectByGraph(String graph) {
        return selectByGraph(valueFactory.createIRI(graph));
    }

    public Statements selectByGraph(Resource graph) {
        Statements result = Statements.arrayList();
        collection.stream()
                .filter((st) -> Objects.equal(graph, st.getContext()))
                .forEach(result::add);
        return result;
    }

    public Statements selectByGraph(Statement statement) {
        return selectByGraph(statement.getContext());
    }

}
