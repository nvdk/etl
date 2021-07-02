package com.linkedpipes.plugin.loader;

import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginJarFile {

    private final File file;

    private final JarFile jarFile;

    private final String jarIri;

    private final String pluginIri;

    private final List<Statement> definition;

    private final List<Statement> configuration;

    private final List<Statement> configurationDescription;

    private final Map<String, JarEntry> dialogEntries;

    public PluginJarFile(
            File file,
            JarFile jarFile,
            String jarIri,
            String pluginIri,
            List<Statement> definition,
            List<Statement> configuration,
            List<Statement> configurationDescription,
            Map<String, JarEntry> dialogEntries) {
        this.file = file;
        this.jarFile = jarFile;
        this.jarIri = jarIri;
        this.pluginIri = pluginIri;
        this.definition = definition;
        this.configuration = configuration;
        this.configurationDescription = configurationDescription;
        this.dialogEntries = dialogEntries;
    }

    public File getFile() {
        return file;
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public String getJarIri() {
        return jarIri;
    }

    public String getPluginIri() {
        return pluginIri;
    }

    public List<Statement> getDefinition() {
        return Collections.unmodifiableList(definition);
    }

    public List<Statement> getConfiguration() {
        return Collections.unmodifiableList(configuration);
    }

    public List<Statement> getConfigurationDescription() {
        return Collections.unmodifiableList(configurationDescription);
    }

    public Map<String, JarEntry> getDialogEntries() {
        return Collections.unmodifiableMap(dialogEntries);
    }
}
