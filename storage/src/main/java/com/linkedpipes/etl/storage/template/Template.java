package com.linkedpipes.etl.storage.template;

public abstract class Template {

    protected String id;

    protected String iri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public abstract boolean isPlugin();

    public abstract boolean isReference();

}
