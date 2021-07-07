package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.template.TemplateException;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.plugin.loader.PluginJarFile;
import com.linkedpipes.plugin.loader.PluginLoader;
import com.linkedpipes.plugin.loader.PluginLoaderException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginService {

    private static final Logger LOG =
            LoggerFactory.getLogger(PluginService.class);

    protected final List<PluginTemplate> pluginTemplates = new ArrayList<>();

    protected final Map<String, PluginJarFile> plugins = new HashMap<>();

    protected final TemplateStore store;

    public PluginService(TemplateStore store) {
        this.store = store;
    }

    public void initialize(File directory)
            throws StoreException, TemplateException {
        LOG.info("Loading plugins ...");
        PluginLoader loader = new PluginLoader();
        List<File> files = listPluginFiles(directory);
        for (File file : files) {
            List<PluginJarFile> references;
            try {
                references = loader.loadPlugin(file);
            } catch (PluginLoaderException ex) {
                LOG.error("Can't load plugin from: {}", file, ex);
                continue;
            }
            for (PluginJarFile plugin : references) {
                createPlugin(plugin);
            }
        }
        LOG.info(
                "Loading plugins ... done (loaded {})",
                pluginTemplates.size());
    }

    protected List<File> listPluginFiles(File directory) {
        return FileUtils.listFiles(directory, new String[]{"jar"}, true)
                .stream().filter(this::isPluginFile)
                .collect(Collectors.toList());
    }

    protected boolean isPluginFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(".jar");
    }

    protected void createPlugin(PluginJarFile reference)
            throws StoreException, TemplateException {
        PluginTemplateLoader loader = new PluginTemplateLoader();
        try {
            loader.load(reference);
        } catch (TemplateException ex) {
            LOG.info("Can't load plugin template: '{}' from '{}'",
                    reference.getPluginIri(), reference.getFile());
            throw ex;
        }
        store.setPlugin(
                loader.getIdentifier(),
                loader.getDefinitionStatements(),
                loader.getConfigurationStatements(),
                loader.getConfigurationDescriptionStatements());
        for (Map.Entry<String, byte[]> entry : loader.getFiles().entrySet()) {
            store.setPluginFile(
                    loader.getIdentifier(),
                    entry.getKey(),
                    entry.getValue());
        }
        PluginDefinition definition = loader.getDefinition();
        PluginTemplate template = new PluginTemplate(
                loader.getIdentifier(), loader.getIri(),
                ((Literal) definition.supportControl).booleanValue());
        pluginTemplates.add(template);
    }

    public List<PluginTemplate> getPluginTemplates() {
        return Collections.unmodifiableList(pluginTemplates);
    }

    public PluginJarFile getPlugin(String iri) {
        return plugins.get(iri);
    }

}
