package com.linkedpipes.etl.storage.plugin;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.plugin.loader.Plugin;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public interface PluginApi {

    Collection<String> listPlugins()
            throws StorageException;

    Optional<Plugin> getPlugin(String iri)
            throws StorageException;

    Optional<byte[]> getPluginFile(String iri, String path)
            throws StorageException;

}
