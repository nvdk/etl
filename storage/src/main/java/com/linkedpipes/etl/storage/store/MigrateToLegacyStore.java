package com.linkedpipes.etl.storage.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.store.model.StoreInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MigrateToLegacyStore {

    private static final String LEGACY_STORE = ReadOnlyLegacyStore.STORE_NAME;

    private static final Logger LOG =
            LoggerFactory.getLogger(MigrateToLegacyStore.class);

    private final File storeDirectory;

    public MigrateToLegacyStore(File storeDirectory) {
        this.storeDirectory = storeDirectory;
    }

    public boolean shouldMigrateLegacyStore(StoreInfo info) {
        File pipelinesDir = new File(storeDirectory, "pipelines");
        File templatesDir = new File(storeDirectory, "templates");
        boolean legacyDirectoriesExist =
                pipelinesDir.exists() || templatesDir.exists();
        // The migration was started but not finished.
        if (info == null) {
            // Data are not new as there are legacy directories.
            return legacyDirectoriesExist;
        }
        // We can be in a middle of a migration.
        return LEGACY_STORE.equals(info.repository());
    }

    public StoreInfo storeInfo() throws StorageException {
        int version = readVersion();
        return new StoreInfo(version, ReadOnlyLegacyStore.STORE_NAME);
    }

    public void migrateData(StoreInfo info) throws StorageException {
        LOG.info("Migrating data to legacy store ...");
        String directoryName = "v" + String.format("%02d", info.version())
                + "-" + ReadOnlyLegacyStore.STORE_NAME;
        File target = new File(storeDirectory, directoryName);
        target.mkdirs();
        saveMove(
                new File(storeDirectory, "knowledge"),
                new File(target, "knowledge"));
        saveMove(
                new File(storeDirectory, "pipelines"),
                new File(target, "pipelines"));
        saveMove(
                new File(storeDirectory, "templates"),
                new File(target, "templates"));
        LOG.info("Migrating data to legacy store ... done");
    }

    private int readVersion() throws StorageException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        File file = repositoryFile();
        if (!file.exists()) {
            return 0;
        }
        try {
            root = mapper.readTree(file);
        } catch (IOException ex) {
            throw new StorageException("Can't read template info file.", ex);
        }
        if (root.has("version")) {
            return root.get("version").asInt();
        }
        return 0;
    }

    private File repositoryFile() {
        File directory = new File(storeDirectory, "templates");
        return new File(directory, "repository-info.json");
    }

    private void saveMove(File source, File target) throws StorageException {
        if (!source.exists()) {
            target.mkdirs();
            return;
        }
        try {
            Files.move(source.toPath(), target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new StorageException("Can't move data.", ex);
        }
    }


}
