package com.linkedpipes.etl.storage.template.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class StoreInfoAdapter {

    private static final Logger LOG =
            LoggerFactory.getLogger(StoreInfoAdapter.class);

    public static StoreInfo loadForStore(File storeDirectory) {
        File file = repositoryFile(storeDirectory);
        if (!file.exists()) {
            return new StoreInfo();
        }
        JsonNode root = loadFromFile(file);
        if (root == null) {
            return new StoreInfo();
        }
        return loadFromJsonNode(root);
    }

    protected static File repositoryFile(File storeDirectory) {
        File directory = new File(storeDirectory, "templates");
        return new File(directory, "repository-info.json");
    }

    protected static JsonNode loadFromFile(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(file);
        } catch (IOException ex) {
            LOG.info("Can't read repository info file.", ex);
            return null;
        }
    }

    protected static StoreInfo loadFromJsonNode(JsonNode root) {
        StoreInfo result = new StoreInfo();
        if (root.has("version")) {
            int version = root.get("version").asInt();
            result.templateVersion = version;
        }
        if (root.has("templateVersion")) {
            int version = root.get("templateVersion").asInt();
            result.templateVersion = version;
        }
        if (root.has("repository")) {
            result.repository = root.get("repository").asText();
        }
        return result;
    }

    public static void saveForStore(File storeDirectory, StoreInfo info)
            throws StoreException {
        File file = repositoryFile(storeDirectory);
        ObjectMapper mapper = new ObjectMapper();
        try {
        mapper.writeValue(file, info);
        } catch (IOException ex) {
            throw new StoreException("Can't save store info.", ex);
        }
    }

}
