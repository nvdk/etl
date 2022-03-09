package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.Collection;
import java.util.Date;

/**
 * Interface for building statements from code.
 */
public class StatementsBuilder extends Statements {

    private Resource defaultGraph = null;

    public StatementsBuilder(Collection<Statement> collection) {
        super(collection);
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


}
