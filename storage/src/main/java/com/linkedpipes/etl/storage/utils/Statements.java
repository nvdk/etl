package com.linkedpipes.etl.storage.utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Statements implements Collection<Statement> {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Collection<Statement> collection;

    private Resource defaultGraph;

    protected Statements(Collection<Statement> collection) {
        this.collection = collection;
    }

    public static Statements wrap(Collection<Statement> collection) {
        if (collection == null) {
            return arrayList();
        }
        return new Statements(collection);
    }

    public static Statements set() {
        return new Statements(new HashSet<>());
    }

    public static Statements arrayList() {
        return new Statements(new ArrayList<>());
    }

    public static Statements arrayList(File file) throws IOException {
        Statements result = new Statements(new ArrayList<>());
        result.addAll(file);
        return result;
    }

    public static Statements arrayList(int size) {
        return new Statements(new ArrayList<>(size));
    }

    public static Statements readOnly() {
        return new Statements(Collections.emptyList());
    }

    public static Statements arrayList(Map<?, Statements> map) {
        Statements result = new Statements(new ArrayList<>());
        map.values().forEach(result::addAll);
        return result;
    }

    public Statements withGraph(String graph) {
        return withGraph(valueFactory.createIRI(graph));
    }

    public Statements withGraph(Resource graph) {
        List<Statement> result = new ArrayList<>(collection.size());
        collection.stream()
                .map(statement -> valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject(),
                        graph
                ))
                .forEach(result::add);
        return wrap(result);
    }

    /**
     * If no graph is provided when adding into this colleciotn, this
     * graph is used.
     */
    public void setDefaultGraph(Resource defaultGraph) {
        this.defaultGraph = defaultGraph;
    }

    public void addIri(String s, String p, String o) {
        addIri(valueFactory.createIRI(s), p, o);
    }

    public void addIri(Resource s, String p, String o) {
        add(s, p, valueFactory.createIRI(o));
    }

    public void addIri(String s, IRI p, String o) {
        addIri(valueFactory.createIRI(s), p, o);
    }

    public void addIri(Resource s, IRI p, String o) {
        add(s, p, valueFactory.createIRI(o));
    }

    public void addString(String s, String p, String o) {
        addString(valueFactory.createIRI(s), p, o);
    }

    public void addString(Resource s, String p, String o) {
        add(s, p, valueFactory.createLiteral(o));
    }

    public void addString(String s, IRI p, String o) {
        add(valueFactory.createIRI(s), p, valueFactory.createLiteral(o));
    }

    public void addString(Resource s, IRI p, String o) {
        add(s, p, valueFactory.createLiteral(o));
    }

    public void addInt(String s, String p, int o) {
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void addInt(Resource s, String p, int o) {
        add(s, p, valueFactory.createLiteral(o));
    }

    public void addInt(Resource s, IRI p, int o) {
        add(s, p, valueFactory.createLiteral(o));
    }

    public void addBoolean(String s, String p, boolean o) {
        addBoolean(s, valueFactory.createIRI(p), o);
    }

    public void addBoolean(Resource s, String p, boolean o) {
        addBoolean(s, valueFactory.createIRI(p), o);
    }

    public void addBoolean(String s, IRI p, boolean o) {
        addBoolean(valueFactory.createIRI(s), p, o);
    }

    public void addBoolean(Resource s, IRI p, boolean o) {
        add(s, p, valueFactory.createLiteral(o));
    }

    public void addDate(String s, String p, Date o) {
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void addDate(Resource s, String p, Date o) {
        addDate(s, valueFactory.createIRI(p), o);
    }

    public void addDate(Resource s, IRI p, Date o) {
        add(s, p, valueFactory.createLiteral(o));
    }

    public void addLong(String s, String p, Long o) {
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void addLong(Resource s, String p, Long o) {
        add(s, valueFactory.createIRI(p), valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, Value o) {
        add(s, valueFactory.createIRI(p), o);
    }

    public void add(Resource s, IRI p, Value o) {
        if (o == null) {
            return;
        }
        collection.add(valueFactory.createStatement(
                s, p, o, defaultGraph));
    }

    @Override
    public boolean add(Statement statement) {
        return collection.add(statement);
    }

    public boolean addAll(Collection<Statement> statements, Resource graph) {
        boolean result = false;
        for (Statement statement : statements) {
            Statement integratedStatement = valueFactory.createStatement(
                    statement.getSubject(),
                    statement.getPredicate(),
                    statement.getObject(),
                    graph);
            result |= collection.add(integratedStatement);
        }
        return result;
    }

    public void addAll(File file) throws IOException {
        Optional<RDFFormat> format =
                Rio.getParserFormatForFileName(file.getName());
        if (format.isEmpty()) {
            throw new IOException("Can't get format for: " + file.getName());
        }
        addAll(file, format.get());
    }

    @Override
    public boolean addAll(Collection<? extends Statement> collection) {
        if (collection == null) {
            return false;
        }
        return this.collection.addAll(collection);
    }

    public void addAll(File file, RDFFormat format) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            addAll(stream, format);
        }
    }

    public void addAll(InputStream stream, RDFFormat format)
            throws IOException {
        try {
            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new StatementCollector(collection));
            parser.parse(stream, "http://localhost/base");
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        }
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

    public Collection<Resource> selectSubjectsIri(IRI p, String o) {
        return selectSubjects(p, valueFactory.createIRI(o));
    }

    public Collection<Resource> selectSubjects(IRI p, Value o) {
        return select(null, p, o).subjects();
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
                .filter((st) -> graph.equals(st.getContext()))
                .forEach(result.collection::add);
        return result;
    }

    public Map<Resource, Statements> splitByGraph() {
        Map<Resource, Statements> result = new HashMap<>();
        for (Statement statement : collection) {
            Resource graph = statement.getContext();
            result.computeIfAbsent(graph, (key) -> Statements.arrayList())
                    .add(statement);
        }
        return result;
    }

    public Collection<Statement> asCollection() {
        return collection;
    }

    public List<Statement> asList() {
        if (collection instanceof List) {
            return (List<Statement>) collection;
        }
        return new ArrayList<>(collection);
    }

    public Collection<Resource> subjects() {
        Set<Resource> result = new HashSet<>();
        for (Statement statement : collection) {
            result.add(statement.getSubject());
        }
        return result;
    }

    public Collection<Value> objects() {
        Set<Value> result = new HashSet<>();
        for (Statement statement : collection) {
            result.add(statement.getObject());
        }
        return result;
    }

    public void replaceSubject(Resource oldSubject, Resource newSubject) {
        List<Statement> toUpdate = collection.stream()
                .filter(statement -> oldSubject.equals(statement.getSubject()))
                .collect(Collectors.toList());
        collection.removeAll(toUpdate);
        toUpdate.stream()
                .map(statement -> valueFactory.createStatement(
                        newSubject,
                        statement.getPredicate(),
                        statement.getObject(),
                        statement.getContext()
                ))
                .forEach(collection::add);
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        return collection.contains(object);
    }

    @Override
    public Iterator<Statement> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return collection.toArray(array);
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.collection.containsAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.collection.removeAll(collection);
    }

    public boolean removeAllForSubjects(Collection<Resource> s) {
        return collection.removeIf(
                statement -> s.contains(statement.getSubject()));
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.collection.retainAll(collection);
    }

    @Override
    public void clear() {
        collection.clear();
    }

}
