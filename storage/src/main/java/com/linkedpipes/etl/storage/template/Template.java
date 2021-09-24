package com.linkedpipes.etl.storage.template;

/**
 * Common abstract claas for templates.
 */
public abstract class Template {

    protected String iri;

    public Template(String iri) {
        this.iri = iri;
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
