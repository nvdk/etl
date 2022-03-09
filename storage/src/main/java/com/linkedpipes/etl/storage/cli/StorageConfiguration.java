package com.linkedpipes.etl.storage.cli;

import java.io.File;

public record StorageConfiguration(
        /*
         * Where should we store storage data.
         */
        File storeDirectory,
        /*
         * Directory where should we look for plugins.
         */
        File pluginDirectory,
        /*
         * Base URL without the "/resources" part.
         */
        String baseUrl,
        /*
         * Port for running web-server.
         */
        Integer port
) {

    public StorageConfiguration(
            StorageConfiguration configuration,
            String storeDirectory,
            String pluginDirectory,
            String baseUrl,
            Integer port) {
        this(
                storeDirectory == null ? configuration.storeDirectory :
                        new File(storeDirectory),
                pluginDirectory == null ? configuration.pluginDirectory :
                        new File(pluginDirectory),
                baseUrl == null ? configuration.baseUrl :
                        baseUrl,
                port == null ? configuration.port :
                        port
        );
    }

}
