package com.linkedpipes.etl.storage.cli;

import com.linkedpipes.etl.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class StorageConfigurationLoader {

    private static final Logger LOG =
            LoggerFactory.getLogger(StorageConfigurationLoader.class);

    private StorageConfiguration configuration;

    public String[] load(String[] args) throws StorageException {
        createDefault();
        String[] nextArgs = loadFromArgs(args);
        loadFileFromEnvironment();
        loadFromEnvironment();
        return nextArgs;
    }

    private void createDefault() {
        configuration = new StorageConfiguration(null, null, null, null);
    }

    private String[] loadFromArgs(String[] args) throws StorageException {
        List<String> result = new ArrayList<>(Arrays.asList(args));
        for (int index = 0; index < result.size(); ++index) {
            String item = result.get(index);
            if (item.startsWith("--configuration-file")) {
                String[] tokens = item.split("=", 2);
                if (tokens.length < 2) {
                    throw new StorageException("Invalid argument: {}", item);
                }
                loadFromFile(new File(tokens[1]));
                result.remove(index);
                return result.toArray(new String[0]);
            }
            if (!item.startsWith("-")) {
                // Read only till the command.
                break;
            }
        }
        return result.toArray(new String[0]);
    }

    private void loadFromFile(File file) throws StorageException {
        LOG.info("Reading configuration from: {}", file);
        String fileName = file.getName();
        if (fileName.endsWith(".properties")) {
            loadPropertiesFile(file);
        } else {
            throw new StorageException("Unknown configuration file type.");
        }
    }

    private void loadPropertiesFile(File file) throws StorageException {
        Properties properties = new Properties();
        try (var stream = new FileInputStream(file);
             var reader = new InputStreamReader(stream,
                     StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new StorageException("Can't load configuration file.", ex);
        }
        //
        configuration = new StorageConfiguration(
                configuration,
                getProperty(properties, "storage.directory"),
                getProperty(properties, "storage.jars.directory"),
                getProperty(properties, "domain.uri"),
                getPropertyInteger(properties, "storage.port")
        );
    }

    private String getProperty(Properties properties, String name)
            throws StorageException {
        try {
            return properties.getProperty(name);
        } catch (RuntimeException ex) {
            throw new StorageException(
                    "Invalid configuration property: '{}'", name, ex);
        }
    }

    private Integer getPropertyInteger(Properties properties, String name)
            throws StorageException {
        String value = getProperty(properties, name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new StorageException(
                    "Invalid configuration property: '{}'", name);
        }
    }

    private void loadFileFromEnvironment() throws StorageException {
        String file = System.getProperty("configFileLocation");
        if (file == null) {
            return;
        }
        loadFromFile(new File(file));
    }

    private void loadFromEnvironment() throws StorageException {
        configuration = new StorageConfiguration(
                configuration,
                getEnv("LP_ETL_STORAGE_DATA"),
                getEnv("LP_ETL_STORAGE_PLUGINS"),
                getEnv("LP_ETL_DOMAIN"),
                getEnvInteger("LP_ETL_STORAGE_PORT")
        );
    }

    private String getEnv(String name) {
        return System.getProperty(name);
    }

    private Integer getEnvInteger(String name) throws StorageException {
        String value = getEnv(name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new StorageException(
                    "Invalid configuration property: '{}'", name);
        }
    }

    public StorageConfiguration getConfiguration() {
        return configuration;
    }

}
