package com.linkedpipes.etl.storage.store.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class StoreInfoAdapter {

    private static final Logger LOG =
            LoggerFactory.getLogger(StoreInfoAdapter.class);

    public static StoreInfo load(File storeDirectory) {
        File file = repositoryFile(storeDirectory);
        if (!file.exists()) {
            return null;
        }
        JsonNode root = loadFromFile(file);
        if (root == null) {
            return null;
        }
        return loadFromJsonNode(root);
    }

    private static File repositoryFile(File storeDirectory) {
        return new File(storeDirectory, "storage-info.json");
    }

    private static JsonNode loadFromFile(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(file);
        } catch (IOException ex) {
            LOG.info("Can't read repository info file.", ex);
            return null;
        }
    }

    private static StoreInfo loadFromJsonNode(JsonNode root) {
        int version = 0;
        String repository = "";
        if (root.has("version")) {
            version = root.get("version").asInt();
        }
        if (root.has("repository")) {
            repository = root.get("repository").asText();
        }
        return new StoreInfo(version, repository);
    }

    public static void save(File storeDirectory, StoreInfo info)
            throws StorageException {
        File file = repositoryFile(storeDirectory);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, info);
        } catch (IOException ex) {
            throw new StorageException("Can't save storage info.", ex);
        }
    }

}
