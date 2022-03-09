package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;

import java.util.Collection;
import java.util.Optional;

public interface PluginTemplateApi {

    Collection<String> listPluginTemplates() throws StorageException;

    Optional<PluginTemplate> loadPluginTemplate(String iri)
            throws StorageException;

}
