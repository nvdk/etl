package com.linkedpipes.etl.storage.plugin;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.plugin.loader.Plugin;
import com.linkedpipes.plugin.loader.PluginLoader;
import com.linkedpipes.plugin.loader.PluginLoaderException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginService implements PluginApi {

    private static final Logger LOG =
            LoggerFactory.getLogger(PluginService.class);

    private final File directory;

    /**
     * For as plugin IRI store given plugin.
     */
    private final Map<String, Plugin> plugins = new HashMap<>();

    public PluginService(File directory) {
        this.directory = directory;
    }

    /**
     * Load all available plugins.
     */
    public void initialize() {
        if (!directory.exists()) {
            LOG.warn("The plugin directory does not exist.");
            return;
        }
        for (File file : listPluginFiles(directory)) {
            if (file.isDirectory()) {
                continue;
            }
            loadPluginFromFile(file);
        }
    }

    private List<File> listPluginFiles(File directory) {
        return FileUtils.listFiles(directory, new String[]{"jar"}, true)
                .stream().filter(this::isPluginFile)
                .collect(Collectors.toList());
    }

    private boolean isPluginFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(".jar");
    }

    private void loadPluginFromFile(File file) {
        PluginLoader loader = new PluginLoader();
        List<Plugin> loadedPlugins;
        try {
            loadedPlugins = loader.loadPlugin(file);
        } catch (PluginLoaderException ex) {
            LOG.error("Can't load plugin from '{}'", file, ex);
            return;
        }
        for (Plugin plugin : loadedPlugins) {
            plugins.put(plugin.pluginIri, plugin);
        }
    }

    @Override
    public Collection<String> listPlugins() {
        return plugins.keySet();
    }

    @Override
    public Optional<Plugin> getPlugin(String iri) {
        return Optional.ofNullable(plugins.get(iri));
    }

    @Override
    public Optional<byte[]> getPluginFile(String iri, String path)
            throws StorageException {
        Optional<Plugin> plugin = getPlugin(iri);
        if (plugin.isEmpty()) {
            return Optional.empty();
        }
        JarEntry entry = plugin.get().fileEntries.get(path);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(readJarEntry(plugin.get().jarFile, entry));
    }

    private byte[] readJarEntry(JarFile jar, JarEntry entry)
            throws StorageException {
        try (InputStream stream = jar.getInputStream(entry)) {
            return stream.readAllBytes();
        } catch (IOException ex) {
            throw new StorageException("Can't read jar file entry.", ex);
        }
    }

}
