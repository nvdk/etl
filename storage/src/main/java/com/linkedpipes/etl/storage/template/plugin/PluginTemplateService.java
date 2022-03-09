package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;

import java.util.Collection;
import java.util.Optional;

public class PluginTemplateService implements PluginTemplateApi {

    private final PluginTemplateStore store;

    public PluginTemplateService(PluginTemplateStore store) {
        this.store = store;
    }

    public Collection<String> listPluginTemplates() throws StorageException {
        return store.listPluginTemplates();
    }

    @Override
    public Optional<PluginTemplate> loadPluginTemplate(String iri)
            throws StorageException {
        return store.loadPluginTemplate(iri);
    }

}
