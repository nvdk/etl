package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Represent a thin template that can modify basic component
 * properties and configuration.
 */
public class ReferenceTemplate extends Template
        implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        TYPE = valueFactory.createIRI(LP_PIPELINE.REFERENCE_TEMPLATE);
    }

    /**
     * Template for this template.
     */
    private String template;

    private PluginTemplate coreTemplate;

    public String getTemplate() {
        return template;
    }

    @Override
    public void loadIri(String iri) {
        this.iri = iri;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_TEMPLATE:
                template = value.stringValue();
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public String getIri() {
        return iri;
    }

    public PluginTemplate getCoreTemplate() {
        return coreTemplate;
    }

    public void setCoreTemplate(PluginTemplate coreTemplate) {
        this.coreTemplate = coreTemplate;
    }

    @Override
    public boolean isPlugin() {
        return false;
    }

    @Override
    public boolean isReference() {
        return true;
    }

}
