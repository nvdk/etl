package com.linkedpipes.etl.storage.cli;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.PipelineService;
import com.linkedpipes.etl.storage.plugin.PluginApi;
import com.linkedpipes.etl.storage.plugin.PluginService;
import com.linkedpipes.etl.storage.store.Store;
import com.linkedpipes.etl.storage.store.StoreFactory;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.plugin.LoadPluginTemplates;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateService;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateService;

/**
 * Manage core-components life cycle.
 */
public class ComponentManager {

    private final StorageConfiguration configuration;

    private Store store;

    private PluginApi pluginApi;

    private TemplateApi templateApi;

    private PipelineApi pipelineApi;

    public ComponentManager(StorageConfiguration configuration) {
        this.configuration = configuration;
    }

    public void initialize() throws StorageException {
        store = createStore();
        pluginApi = createPluginApi();
        loadPluginsToStore(store, pluginApi);
        templateApi = createTemplateApi(store);
        pipelineApi = createPipelineApi(store);
    }

    private Store createStore() throws StorageException {
        StoreFactory storeFactory = new StoreFactory(
                configuration.storeDirectory(), configuration.baseUrl());
        Store result;
        try {
            result = storeFactory.createStore();
        } catch (StorageException ex) {
            throw new StorageException("Can't create store.", ex);
        }
        try {
            result.initialize();
        } catch (StorageException ex) {
            throw new StorageException("Can't initialize store.", ex);
        }
        return result;
    }

    private PluginApi createPluginApi() {
        PluginService pluginService =
                new PluginService(configuration.pluginDirectory());
        pluginService.initialize();
        return pluginService;
    }

    private void loadPluginsToStore(Store store, PluginApi pluginApi)
            throws StorageException {
        try {
            (new LoadPluginTemplates(store, pluginApi)).execute();
        } catch (StorageException ex) {
            throw new StorageException("Can't load plugins into store.", ex);
        }
    }

    private TemplateApi createTemplateApi(Store store) {
        return new TemplateFacade(
                new PluginTemplateService(store),
                new ReferenceTemplateService(store));
    }

    private PipelineApi createPipelineApi(Store store) {
        return new PipelineService(store);
    }

    public Store store() {
        return store;
    }

    public PluginApi pluginApi() {
        return pluginApi;
    }

    public TemplateApi templateApi() {
        return templateApi;
    }

    public PipelineApi pipelineApi() {
        return pipelineApi;
    }

    public void shutdown() {
        // For now, we do nothing.
    }

}
