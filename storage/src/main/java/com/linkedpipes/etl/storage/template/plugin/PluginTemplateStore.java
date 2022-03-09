package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;

import java.util.Collection;
import java.util.Optional;

/**
 * Interface for plugin templates.
 */
public interface PluginTemplateStore {

    /**
     * Return IRI of all stored plugin templates.
     */
    Collection<String> listPluginTemplates()
            throws StorageException;

    /**
     * Return full representation of the plugin.
     */
    Optional<PluginTemplate> loadPluginTemplate(String iri)
            throws StorageException;

    void storePluginTemplate(PluginTemplate template)
            throws StorageException;

}
