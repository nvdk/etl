package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.template.Template;

public class PluginTemplate extends Template {

    private final boolean supportControl;

    public PluginTemplate(String id, String iri, boolean supportControl) {
        super(id, iri);
        this.supportControl = supportControl;
    }

    @Override
    public boolean isPlugin() {
        return true;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    public boolean isSupportControl() {
        return supportControl;
    }

}
