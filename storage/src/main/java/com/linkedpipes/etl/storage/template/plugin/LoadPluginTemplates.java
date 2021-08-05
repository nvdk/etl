package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.etl.storage.template.TemplateException;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.plugin.loader.PluginJarFile;
import com.linkedpipes.plugin.loader.PluginLoader;
import com.linkedpipes.plugin.loader.PluginLoaderException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Load PluginTemplates into store and notify listeners.
 */
public class LoadPluginTemplates {

    private static final Logger LOG =
            LoggerFactory.getLogger(LoadPluginTemplates.class);

    public static void apply(
            Configuration configuration,
            TemplateEventListener listener,
            TemplateStore store)
            throws TemplateException, StoreException {
        (new LoadPluginTemplates(configuration, listener, store))
                .loadPluginTemplates();
    }

    private final Configuration configuration;

    private final TemplateEventListener listener;

    private final TemplateStore store;

    protected LoadPluginTemplates(
            Configuration configuration,
            TemplateEventListener listener,
            TemplateStore store) {
        this.configuration = configuration;
        this.listener = listener;
        this.store = store;
    }

    protected void loadPluginTemplates()
            throws TemplateException, StoreException {
        LOG.info("Loading plugins ...");
        PluginLoader loader = new PluginLoader();
        List<File> files = listPluginFiles(configuration.getJarDirectory());
        for (File file : files) {
            List<PluginJarFile> references;
            try {
                references = loader.loadPlugin(file);
            } catch (PluginLoaderException ex) {
                LOG.error("Can't load plugin from: {}", file, ex);
                continue;
            }
            for (PluginJarFile plugin : references) {
                listener.onPluginLoaded(plugin);
                loadPluginTemplate(plugin);
            }
        }
        LOG.info("Loading plugins ... done");
    }

    protected List<File> listPluginFiles(File directory) {
        return FileUtils.listFiles(directory, new String[]{"jar"}, true)
                .stream().filter(this::isPluginFile)
                .collect(Collectors.toList());
    }

    protected boolean isPluginFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(".jar");
    }

    protected void loadPluginTemplate(PluginJarFile pluginJarFile)
            throws StoreException, TemplateException {
        PluginContainer container = createPluginContainer(pluginJarFile);
        store.setPlugin(
                container.identifier,
                container.definitionStatements,
                container.configurationStatements,
                container.configurationDescriptionStatements);
        for (Map.Entry<String, byte[]> entry : container.files.entrySet()) {
            store.setPluginFile(
                    container.identifier,
                    entry.getKey(),
                    entry.getValue());
        }
        listener.onPluginTemplateLoaded(container);

    }

    protected PluginContainer createPluginContainer(
            PluginJarFile pluginJarFile) throws TemplateException {
        try {
            return (new PluginContainerFactory()).create(pluginJarFile);
        } catch (TemplateException ex) {
            LOG.info("Can't load plugin template: '{}' from '{}'",
                    pluginJarFile.getPluginIri(), pluginJarFile.getFile());
            throw ex;
        }
    }

}
