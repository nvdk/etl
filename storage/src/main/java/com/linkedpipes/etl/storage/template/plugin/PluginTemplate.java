package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.template.Template;

public class PluginTemplate extends Template {

    private final boolean supportControl;

    public PluginTemplate(PluginContainer container) {
        super(container.identifier, container.resource.stringValue());
        PluginDefinition definition = container.definition;
        this.supportControl = definition.supportControl.booleanValue();
    }

    @Override
    public boolean isPluginTemplate() {
        return true;
    }

    @Override
    public boolean isReferenceTemplate() {
        return false;
    }

    public boolean isSupportControl() {
        return supportControl;
    }

}
