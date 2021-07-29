package com.linkedpipes.etl.storage.template.store;

import com.linkedpipes.etl.storage.template.store.file.FileStoreV1;
import com.linkedpipes.etl.storage.template.store.legacy.LegacyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TemplateStoreService {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateStoreService.class);

    public static final int LATEST_TEMPLATE_VERSION = 5;

    protected StoreInfo info = new StoreInfo();

    protected final File templateDirectory;

    protected final File storeDirectory;

    public TemplateStoreService(File storeDirectory, File templateDirectory) {
        this.templateDirectory = templateDirectory;
        this.storeDirectory = storeDirectory;
    }

    public void initialize() {
        info = StoreInfoAdapter.load(templateDirectory);
    }

    /**
     * Create store using the info file, i.e. store with the current
     * data in the file system.
     */
    public TemplateStore createStoreFromInfoFile() throws StoreException {
        if (info.repository == null) {
            // For no repository we create backup directory and load
            // it using legacy template.
            String name = "v" + info.templateVersion + "-backup";
            migrateToLegacyRepository(name);
            return new LegacyStore(new File(templateDirectory, name));
        }
        File storeDirectory = getStoreDirectory(
                info.templateVersion, info.repository);
        switch (info.repository) {
            case LegacyStore.STORE_NAME:
                return new LegacyStore(storeDirectory);
            case FileStoreV1.STORE_NAME:
                return new FileStoreV1(storeDirectory);
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
        File directory = createLegacyDirectory(name);
        moveLegacyTemplates(directory);
        LOG.info("Migrating data to legacy store ... done");
    }

    protected File createLegacyDirectory(String name) {
        File result = new File(templateDirectory, name);
        result.mkdirs();
        return result;
    }

    protected void moveLegacyTemplates(File directory) throws StoreException {
        File[] files = templateDirectory.listFiles();
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
            File target = new File(directory, file.getName());
            try {
                Files.move(file.toPath(), target.toPath());
            } catch (IOException ex) {
                throw new StoreException("Can't move a template", ex);
            }
        }
    }

    protected File getStoreDirectory(int templateVersion, String storeName) {
        String name = "v" + templateVersion + "-" + storeName;
        return new File(templateDirectory, name);
    }

    /**
     * Create latest version of store that should be used to store
     * templates. Should be used when migrating to latest repository version.
     */
    public TemplateStore createStore() {
        File storeDirectory = getStoreDirectory(
                LATEST_TEMPLATE_VERSION, FileStoreV1.STORE_NAME);
        return new FileStoreV1(storeDirectory);
    }

    /**
     * Return true if data from old repository should be migrated into
     * a new repository.
     */
    public boolean shouldMigrate() {
        return info.templateVersion != LATEST_TEMPLATE_VERSION ||
                info.repository == null;
    }

    public StoreInfo getStoreInfo() {
        return info.clone();
    }

    public void setStoreInfo(StoreInfo info) throws StoreException {
        try {
            StoreInfoAdapter.save(info, templateDirectory);
        } catch (IOException ex) {
            this.info = info;
            throw new StoreException("Can't save store info.", ex);
        }
    }


}
