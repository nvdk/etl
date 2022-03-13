package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.plugin.PluginApi;
import com.linkedpipes.etl.storage.template.plugin.adapter.PluginTemplateAdapter;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.plugin.loader.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * Load plugin templates into given store.
 */
public class LoadPluginTemplates {

    private static final Logger LOG =
            LoggerFactory.getLogger(LoadPluginTemplates.class);

    private final PluginTemplateStore store;

    private final PluginApi pluginApi;

    public LoadPluginTemplates(PluginTemplateStore store, PluginApi pluginApi) {
        this.store = store;
        this.pluginApi = pluginApi;
    }

    public void execute() throws StorageException {
        for (String iri : pluginApi.listPlugins()) {
            Optional<Plugin> pluginWrap = pluginApi.getPlugin(iri);
            if (pluginWrap.isEmpty()) {
                continue;
            }
            Plugin plugin = pluginWrap.get();
            PluginTemplate templatePlugin =
                    PluginTemplateAdapter.asPluginTemplate(plugin);
            if (templatePlugin == null) {
                LOG.info("No plugin loaded for '{}'.", plugin.pluginIri);
                continue;
            }
            try {
                store.storePluginTemplate(templatePlugin);
            } catch (StorageException ex) {
                LOG.warn("Can't store plugin '{}'.", plugin.pluginIri, ex);
            }
        }
    }

}
