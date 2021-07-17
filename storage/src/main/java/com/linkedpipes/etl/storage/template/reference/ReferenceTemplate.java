package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.template.Template;

/**
 * Represent a thin template that can modify basic component
 * properties and configuration.
 */
public class ReferenceTemplate extends Template {

    private final String template;

    private String corePlugin;

    public ReferenceTemplate(
            String identifier,ReferenceDefinition definition) {
        super(identifier, definition.resource.stringValue());
        this.template = definition.template.stringValue();
    }

    public ReferenceTemplate(ReferenceContainer container) {
        super(container.identifier, container.resource.stringValue());
        this.template = container.definition.template.stringValue();
    }

    public String getTemplate() {
        return template;
    }

    public String getPluginTemplate() {
        return corePlugin;
    }

    public void setCorePlugin(String corePlugin) {
        this.corePlugin = corePlugin;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public boolean getCorePlugin() {
        return false;
    }

    @Override
    public boolean isReference() {
        return true;
    }

}
