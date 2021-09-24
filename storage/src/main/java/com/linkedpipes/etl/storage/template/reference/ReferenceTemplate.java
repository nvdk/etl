package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.template.Template;

/**
 * Represent a thin template that can modify basic component
 * properties and configuration.
 */
public class ReferenceTemplate extends Template {

    private final String template;

    private String rootPluginTemplate;

    public ReferenceTemplate(ReferenceDefinition definition) {
        super(definition.resource.stringValue());
        this.template = definition.template.stringValue();
    }

    public ReferenceTemplate(ReferenceContainer container) {
        super(container.resource.stringValue());
        this.template = container.definition.template.stringValue();
    }

    public String getTemplate() {
        return template;
    }

    public void setRootPluginTemplate(String rootPluginTemplate) {
        this.rootPluginTemplate = rootPluginTemplate;
    }

    public String getRootPluginTemplate() {
        return rootPluginTemplate;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public boolean isPluginTemplate() {
        return false;
    }

    @Override
    public boolean isReferenceTemplate() {
        return true;
    }

}
