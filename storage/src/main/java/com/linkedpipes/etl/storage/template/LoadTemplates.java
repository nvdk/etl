package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.template.migration.MigrateStore;
import com.linkedpipes.etl.storage.template.plugin.LoadPluginTemplates;
import com.linkedpipes.etl.storage.template.reference.LoadReferenceTemplates;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.StoreInfoAdapter;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreFactory;
import org.eclipse.rdf4j.query.algebra.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Create store and load templates.
 */
class LoadTemplates {

    private static final Logger LOG =
            LoggerFactory.getLogger(LoadTemplates.class);

    public static TemplateStore apply(
            Configuration configuration,
            TemplateEventListener listener) throws BaseException {
        return (new LoadTemplates(configuration, listener)).loadTemplates();
    }

    private final Configuration configuration;

    private final TemplateEventListener listener;

    protected LoadTemplates(
            Configuration configuration,
            TemplateEventListener listener) {
        this.configuration = configuration;
        this.listener = listener;
    }

    protected TemplateStore loadTemplates() throws BaseException {
        File storeDirectory = configuration.getStoreDirectory();
        TemplateStoreFactory storeFactory =
                new TemplateStoreFactory(storeDirectory);
        StoreInfo storeInfo = StoreInfoAdapter.loadForStore(storeDirectory);
        TemplateStore result = storeFactory.createStore(storeInfo);
        try {
            LoadPluginTemplates.apply(configuration, listener, result);
            if (shouldMigrate(storeInfo)) {
                result = createCurrentStoreAndMigrateData(
                        storeFactory, result, storeInfo);
            }
            LoadReferenceTemplates.apply(listener, result);
        } catch (Exception ex) {
            LOG.error("Initialization failed.", ex);
            throw ex;
        }
        return result;
    }

    protected boolean shouldMigrate(StoreInfo currentInfo) {
        StoreInfo latestInfo = StoreInfo.CachedFileStore();
        return currentInfo.templateVersion != latestInfo.templateVersion ||
                currentInfo.repository == null ||
                !currentInfo.repository.equals(latestInfo.repository);
    }

    protected TemplateStore createCurrentStoreAndMigrateData(
            TemplateStoreFactory storeFactory,
            TemplateStore sourceStore,
            StoreInfo sourceInfo)
            throws BaseException {
        File storeDirectory = configuration.getStoreDirectory();
        StoreInfo targetInfo = StoreInfo.CachedFileStore();
        TemplateStore targetStore = storeFactory.createStore(targetInfo);
        MigrateStore migration = new MigrateStore(
                sourceStore, targetStore, sourceInfo, storeDirectory);
        migration.migrate();
        StoreInfoAdapter.saveForStore(storeDirectory, targetInfo);
        return targetStore;
    }

}
