package com.linkedpipes.etl.storage.template.store;

import com.linkedpipes.etl.storage.template.store.cached.CachedStoreV1;
import com.linkedpipes.etl.storage.template.store.file.FileStoreV1;
import com.linkedpipes.etl.storage.template.store.legacy.ReadOnlyLegacyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TemplateStoreFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateStoreFactory.class);

    protected final File storeDirectory;

    public TemplateStoreFactory(File storeDirectory) {
        this.storeDirectory = storeDirectory;
    }

    public TemplateStore createStore(StoreInfo info) throws StoreException {
        if (info.repository == null) {
            // For no repository we create backup directory and load
            // it using legacy template.
            String name = "v" + info.templateVersion + "-backup";
            migrateToLegacyRepository(name);
            return new ReadOnlyLegacyStore(
                    new File(getTemplatesDirectory(), name));
        }
        File directory = getTemplatesStoreDirectory(info);
        switch (info.repository) {
            case ReadOnlyLegacyStore.STORE_NAME:
                return new ReadOnlyLegacyStore(directory);
            case FileStoreV1.STORE_NAME:
                return new FileStoreV1(directory);
            case CachedStoreV1.STORE_NAME:
                return new CachedStoreV1(directory);
            default:
                throw new StoreException("Invalid store type.");
        }
    }

    protected File getTemplatesDirectory() {
        return new File(storeDirectory, "templates");
    }

    /**
     * We need to copy all files to a directory for the legacy store.
     */
    protected void migrateToLegacyRepository(
            String name) throws StoreException {
        LOG.info("Migrating data to legacy store ...");
        File directory = createLegacyDirectory(name);
        moveLegacyTemplates(directory);
        LOG.info("Migrating data to legacy store ... done");
    }

    protected File createLegacyDirectory(String name) {
        File result = new File(getTemplatesDirectory(), name);
        result.mkdirs();
        return result;
    }

    protected void moveLegacyTemplates(File directory) throws StoreException {
        File[] files = getTemplatesDirectory().listFiles();
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

    protected File getTemplatesStoreDirectory(StoreInfo info) {
        String name = "v" + info.templateVersion + "-" + info.repository;
        return new File(getTemplatesDirectory(), name);
    }

}
