package com.linkedpipes.etl.storage.store;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.store.model.StoreInfo;
import com.linkedpipes.etl.storage.store.model.StoreInfoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class StoreFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(StoreFactory.class);

    private final File storeDirectory;

    private final String baseUrl;

    public StoreFactory(File storeDirectory, String baseUrl) {
        this.storeDirectory = storeDirectory;
        this.baseUrl = baseUrl;
    }

    public Store createStore() throws StorageException {
        StoreInfo sourceInfo = StoreInfoAdapter.load(storeDirectory);
        MigrateToLegacyStore legacyMigration =
                new MigrateToLegacyStore(storeDirectory);
        if (legacyMigration.shouldMigrateLegacyStore(sourceInfo)) {
            if (sourceInfo == null) {
                sourceInfo = legacyMigration.storeInfo();
            }
            // Save current info if migration fail, so we can recover.
            StoreInfoAdapter.save(storeDirectory, sourceInfo);
            legacyMigration.migrateData(sourceInfo);
        }
        StoreInfo targetInfo = createStoreInfo();
        Store targetStore = createStore(targetInfo);
        targetStore.initialize();
        if (shouldMigrate(sourceInfo, targetInfo)) {
            Store currentStore = createStore(sourceInfo);
            currentStore.initialize();
            LOG.info("Migrating store from v{} {} to v{} {} ...",
                    sourceInfo.version(), sourceInfo.repository(),
                    targetInfo.version(), targetInfo.repository());
            (new MigrateStore(currentStore, targetStore)).migrate();
            LOG.info("Store Migration finished");
        }
        StoreInfoAdapter.save(storeDirectory, targetInfo);
        LOG.info("Store v{} {} initialized in '{}'",
                targetInfo.version(), targetInfo.repository(),
                storeDirectory.getAbsolutePath());
        return targetStore;
    }

    private StoreInfo createStoreInfo() {
        return new StoreInfo(StoreInfo.CURRENT_VERSION,
                RdfDirectoryStoreV1.STORE_NAME);
    }

    private Store createStore(StoreInfo info) throws StorageException {
        File directory = new File(storeDirectory,
                "v" + String.format("%02d", info.version())
                        + "-" + info.repository());
        return switch (info.repository()) {
            case ReadOnlyLegacyStore.STORE_NAME -> new ReadOnlyLegacyStore(
                    directory, baseUrl, info.version());
            case RdfDirectoryStoreV1.STORE_NAME -> new RdfDirectoryStoreV1(
                    directory, baseUrl);
            default -> throw new StorageException("Invalid store specification");
        };
    }

    private boolean shouldMigrate(StoreInfo source, StoreInfo target) {
        return source != null && !source.equals(target);
    }

}
