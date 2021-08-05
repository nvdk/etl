package com.linkedpipes.etl.storage.template;

/**
 * Common abstract claas for templates.
 */
public abstract class Template {

    protected String id;

    protected String iri;

    public Template(String id, String iri) {
        this.id = id;
        this.iri = iri;
    }

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

    public abstract boolean isPluginTemplate();

    public abstract boolean isReferenceTemplate();

}
