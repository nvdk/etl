package com.linkedpipes.etl.storage.template.store.cached;

import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.file.FileStoreV1;
import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedStoreV1 implements TemplateStore {

    private static class TemplateData {

        public List<Statement> definition =
                Collections.emptyList();

        public List<Statement> configuration =
                Collections.emptyList();

        public List<Statement> configurationDescription =
                Collections.emptyList();

        public Map<String, byte[]> files =
                new HashMap<>();

    }

    public static final String STORE_NAME = "memory-cached-file-v1";

    protected final FileStoreV1 store;

    protected final Map<String, TemplateData> cache = new HashMap<>();

    public CachedStoreV1(File directory) {
        this.store = new FileStoreV1(directory);
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public List<String> getReferenceIdentifiers() {
        return store.getReferenceIdentifiers();
    }

    @Override
    public String reserveIdentifier() throws StoreException {
        return store.reserveIdentifier();
    }

    @Override
    public List<Statement> getPluginDefinition(
            String id) throws StoreException {
        return getPluginData(id).definition;
    }

    /**
     * Plugin templates are saved only in main memory.
     */
    protected TemplateData getPluginData(String id) throws StoreException {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        throw new StoreException("Missing plugin template");
    }

    @Override
    public void setPlugin(
            String id,
            Collection<Statement> definition,
            Collection<Statement> configuration,
            Collection<Statement> configurationDescription) {
        TemplateData templateData = new TemplateData();
        templateData.definition =
                new ArrayList<>(definition);
        templateData.configuration =
                new ArrayList<>(configuration);
        templateData.configurationDescription =
                new ArrayList<>(configurationDescription);
        cache.put(id, templateData);
    }

    @Override
    public List<Statement> getReferenceDefinition(
            String id) throws StoreException {
        loadReferenceToCache(id);
        return cache.get(id).definition;
    }

    protected void loadReferenceToCache(String id) throws StoreException {
        if (cache.containsKey(id)) {
            return;
        }
        TemplateData result = new TemplateData();
        result.definition = store.getReferenceDefinition(id);
        result.configuration = store.getReferenceConfiguration(id);
        cache.put(id, result);
    }

    @Override
    public void setReferenceDefinition(
            String id, Collection<Statement> statements) throws StoreException {
        store.setReferenceDefinition(id, statements);
        loadReferenceToCache(id);
        cache.get(id).definition = new ArrayList<>(statements);
    }

    @Override
    public List<Statement> getPluginConfiguration(
            String id) throws StoreException {
        return getPluginData(id).configuration;
    }

    @Override
    public List<Statement> getReferenceConfiguration(
            String id) throws StoreException {
        loadReferenceToCache(id);
        return cache.get(id).configuration;
    }

    @Override
    public void setReferenceConfiguration(
            String id, Collection<Statement> statements
    ) throws StoreException {
        store.setReferenceConfiguration(id, statements);
        loadReferenceToCache(id);
        cache.get(id).configuration = new ArrayList<>(statements);
    }

    @Override
    public List<Statement> getPluginConfigurationDescription(
            String id) throws StoreException {
        return getPluginData(id).configurationDescription;
    }

    @Override
    public byte[] getPluginFile(String id, String path) throws StoreException {
        return getPluginData(id).files.get(path);
    }

    @Override
    public void setPluginFile(
            String id, String path, byte[] content) throws StoreException {
        getPluginData(id).files.put(path, content);
    }

    @Override
    public void removeReference(String id) throws StoreException {
        cache.remove(id);
        store.removeReference(id);
    }

}
