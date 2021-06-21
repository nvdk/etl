package com.linkedpipes.etl.storage.template.store;

import com.linkedpipes.etl.storage.template.store.legacy.LegacyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TemplateStoreService {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateStoreService.class);

    public static final int LATEST_VERSION = 4;

    protected StoreInfo info = new StoreInfo();

    protected File directory;

    public TemplateStoreService(File directory) {
        this.directory = directory;
    }

    public void initialize() {
        info = StoreInfoAdapter.load(directory);
    }

    /**
     * Create store using the info file.
     */
    public TemplateStore createStoreFromInfoFile() throws StoreException {
        if (info.repository == null) {
            // For no repository we create backup directory and load
            // it using legacy template.
            String name = "v" + info.templateVersion + "-backup";
            migrateToLegacyRepository(name);
            return new LegacyStore(new File(directory, name));
        }
        switch (info.repository) {
            case LegacyStore.STORE_NAME:
                return createLegacyRepository(info.templateVersion);
            default:
                throw new StoreException("Invalid store type.");
        }
    }

    /**
     * We need to copy all files to a directory for the legacy store.
     */
    protected void migrateToLegacyRepository(String name) throws StoreException {
        LOG.info("Migrating data to legacy store ...");
        info.repository = LegacyStore.STORE_NAME;
        File storeDirectory = new File(directory, name);
        storeDirectory.mkdirs();
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                continue;
            }
            String fileName = file.getName();
            if (fileName.startsWith("v")) {
                // New storage version file.
                continue;
            }
            File target = new File(storeDirectory, file.getName());
            try {
                Files.move(file.toPath(), target.toPath());
            } catch (IOException ex) {
                throw new StoreException("Can't move a template", ex);
            }
        }
        LOG.info("Migrating data to legacy store ... done");
    }

    protected TemplateStore createLegacyRepository(int version) {
        String name = "v" + version + "-" + LegacyStore.STORE_NAME;
        File storeDirectory = new File(directory, name);
        return new LegacyStore(storeDirectory);
    }

    /**
     * Create store using current configuration.
     */
    public TemplateStore createStore() {
        return createLegacyRepository(LATEST_VERSION);
    }

    /**
     * Return true if data from old repository should be migrated into
     * a nre repository.
     */
    public boolean shouldMigrate() {
        return info.templateVersion != LATEST_VERSION &&
                info.repository != null;
    }

    public StoreInfo getStoreInfo() {
        return info.clone();
    }

    public void setStoreInfo(StoreInfo info) throws StoreException {
        try {
            StoreInfoAdapter.save(info, directory);
        } catch (IOException ex) {
            this.info = info;
            throw new StoreException("Can't save store info.", ex);
        }
    }


}
