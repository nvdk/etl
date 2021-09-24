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
    public List<String> getReferencesIri() {
        return store.getReferencesIri();
    }

    @Override
    public String reserveIri(String domain) throws StoreException {
        return store.reserveIri(domain);
    }

    @Override
    public List<Statement> getPluginDefinition(
            String iri) throws StoreException {
        return getPluginData(iri).definition;
    }

    /**
     * Plugin templates are saved only in main memory.
     */
    protected TemplateData getPluginData(String iri) throws StoreException {
        if (cache.containsKey(iri)) {
            return cache.get(iri);
        }
        throw new StoreException("Missing plugin template");
    }

    @Override
    public void setPlugin(
            String iri,
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
        cache.put(iri, templateData);
    }

    @Override
    public List<Statement> getReferenceDefinition(
            String iri) throws StoreException {
        loadReferenceToCache(iri);
        return cache.get(iri).definition;
    }

    protected void loadReferenceToCache(String iri) throws StoreException {
        if (cache.containsKey(iri)) {
            return;
        }
        TemplateData result = new TemplateData();
        result.definition = store.getReferenceDefinition(iri);
        result.configuration = store.getReferenceConfiguration(iri);
        cache.put(iri, result);
    }

    @Override
    public void setReferenceDefinition(
            String iri, Collection<Statement> statements)
            throws StoreException {
        store.setReferenceDefinition(iri, statements);
        loadReferenceToCache(iri);
        cache.get(iri).definition = new ArrayList<>(statements);
    }

    @Override
    public List<Statement> getPluginConfiguration(
            String iri) throws StoreException {
        return getPluginData(iri).configuration;
    }

    @Override
    public List<Statement> getReferenceConfiguration(
            String iri) throws StoreException {
        loadReferenceToCache(iri);
        return cache.get(iri).configuration;
    }

    @Override
    public void setReferenceConfiguration(
            String iri, Collection<Statement> statements)
            throws StoreException {
        store.setReferenceConfiguration(iri, statements);
        loadReferenceToCache(iri);
        cache.get(iri).configuration = new ArrayList<>(statements);
    }

    @Override
    public List<Statement> getPluginConfigurationDescription(
            String iri) throws StoreException {
        return getPluginData(iri).configurationDescription;
    }

    @Override
    public byte[] getPluginFile(
            String iri, String path) throws StoreException {
        return getPluginData(iri).files.get(path);
    }

    @Override
    public void setPluginFile(
            String iri, String path, byte[] content) throws StoreException {
        getPluginData(iri).files.put(path, content);
    }

    @Override
    public void removeReference(String iri) throws StoreException {
        cache.remove(iri);
        store.removeReference(iri);
    }

}
