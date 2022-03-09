package com.linkedpipes.plugin.loader;

import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Single JAR file can host multiple plugins.
 */
public class Plugin {

    /**
     * Path to the JAR file in secondary memory.
     */
    public final File file;

    public final JarFile jarFile;

    public final String jarIri;

    public final String pluginIri;

    public final List<Statement> definition;

    public final List<Statement> configuration;

    public final List<Statement> configurationDescription;

    public final Map<String, JarEntry> fileEntries;

    public Plugin(
            File file,
            JarFile jarFile,
            String iri,
            String pluginIri,
            List<Statement> definition,
            List<Statement> configuration,
            List<Statement> configurationDescription,
            Map<String, JarEntry> fileEntries) {
        this.file = file;
        this.jarFile = jarFile;
        this.jarIri = iri;
        this.pluginIri = pluginIri;
        this.definition =
                Collections.unmodifiableList(definition);
        this.configuration =
                Collections.unmodifiableList(configuration);
        this.configurationDescription =
                Collections.unmodifiableList(configurationDescription);
        this.fileEntries =
                Collections.unmodifiableMap(fileEntries);
    }

}
