package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.jar.JarComponent;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Copy template definition from a JAR file to a repository. This class
 * is not thread save.
 */
public  class PluginTemplateFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(PluginTemplateFactory.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final TemplateStore store;

    private JarFile jarFile;

    private Map<String, JarEntry> entries;

    public PluginTemplateFactory(TemplateStore store) {
        this.store = store;
    }

    public void create(JarComponent component) {
        try {
            this.jarFile = new JarFile(component.getFile());
            loadJarEntries(component.getFile());
        } catch (Exception ex) {
            LOG.error("Can't read jar template: {}", component.getIri(), ex);
            return;
        }
        String id = "jar-" + component.getFile().getName();
        try {
            copyStaticFiles(id);
            Collection<Statement> definition = loadDefinition();
            Resource resource = getTemplateResource(definition);
            if (resource == null) {
                throw new BaseException("Can't read template resource.");
            }
            Collection<String> dialogs = copyDialogFiles(id);
            definition.addAll(createDefinitionForDialogs(resource, dialogs));
            // Handle configuration.
            Collection<Statement> config = loadConfig();
            Resource configGraph = this.valueFactory.createIRI(
                    resource.stringValue() + "/configGraph");
            config = RdfUtils.forceContext(config, configGraph);
            store.setPluginConfiguration(id, config);
            // Handle configuration description.
            Collection<Statement> configDesc = loadConfigDescription();
            Resource configDescGraph = this.valueFactory.createIRI(
                    resource.stringValue() + "/configDescGraph");
            configDesc = RdfUtils.forceContext(configDesc, configDescGraph);
            store.setPluginConfigurationDescription(id, configDesc);
            // Finalize definition and interface.
            definition = RdfUtils.forceContext(definition, resource);
            store.setPluginInterface(id, definition);
            definition.add(valueFactory.createStatement(resource,
                    valueFactory.createIRI(LP_PIPELINE.HAS_CONFIGURATION_GRAPH),
                    configGraph, resource
            ));
            store.setPluginDefinition(id, definition);
        } catch (Exception ex) {
            LOG.error("Can't import jar template: {}", component.getIri(), ex);
            try {
                store.removePlugin(id);
            } catch (StoreException storeException) {
                LOG.warn("Can't delete after failure.", storeException);
            }
        }
    }

    private void loadJarEntries(File path) throws IOException {
        this.entries = new HashMap<>();
        try (JarFile jar = new JarFile(path)) {
            for (Enumeration<JarEntry> enums = jar.entries();
                    enums.hasMoreElements(); ) {
                JarEntry entry = enums.nextElement();
                String name = entry.getName();
                boolean isDirectory = name.endsWith("/");
                if (isDirectory || !name.startsWith("LP-ETL/template/")) {
                    continue;
                }
                String key = name.substring("LP-ETL/template/".length());
                this.entries.put(key, entry);
            }
        }
    }

    private Collection<Statement> loadDefinition() throws BaseException {
        JarEntry entry = selectEntryByPrefix("definition.");
        return readAsRdf(entry);
    }

    private JarEntry selectEntryByPrefix(String prefix) throws BaseException {
        for (Map.Entry<String, JarEntry> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                return entry.getValue();
            }
        }
        throw new BaseException("Missing jar file entry: {}", prefix);
    }

    private Collection<Statement> loadConfig() throws BaseException {
        JarEntry entry = selectEntryByPrefix("config.");
        return readAsRdf(entry);
    }

    private Collection<Statement> loadConfigDescription() throws BaseException {
        JarEntry entry = selectEntryByPrefix("config-desc.");
        return readAsRdf(entry);
    }

    private void copyStaticFiles(String id) throws IOException, StoreException {
        for (Map.Entry<String, JarEntry> entry : entries.entrySet()) {
            if (!entry.getKey().startsWith("static")) {
                return;
            }
            String path = entry.getKey().substring("static/".length());
            copyEntry(entry.getValue(), id, "static/" + path);
        }
    }

    private Collection<String> copyDialogFiles(String id)
            throws IOException, StoreException {
        Set<String> dialogNames = new HashSet<>();
        for (Map.Entry<String, JarEntry> entry : entries.entrySet()) {
            if (!entry.getKey().startsWith("dialog/")) {
                continue;
            }
            String path = entry.getKey().substring("dialog/".length());
            String dialogName = path.substring(0, path.indexOf("/"));
            copyEntry(entry.getValue(), id, "dialog/" + path);
            dialogNames.add(dialogName);
        }
        return dialogNames;
    }

    private void copyEntry(JarEntry entry, String id, String destination)
            throws IOException, StoreException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        try (InputStream stream = this.jarFile.getInputStream(entry)) {
            int readSize;
            while ((readSize = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, readSize);
            }
        }
        byte[] content = buffer.toByteArray();
        store.setPluginFile(id, destination, content);
    }

    private Collection<Statement> readAsRdf(
            JarEntry entry) throws BaseException {
        try (InputStream stream = jarFile.getInputStream(entry)) {
            return RdfUtils.read(stream, formatForEntry(entry));
        } catch (IOException | BaseException ex) {
            throw new BaseException("Can't load definition: {}",
                    entry.getName(), ex);
        }
    }

    private RDFFormat formatForEntry(
            JarEntry entry) throws RdfUtils.RdfException {
        return RdfUtils.getFormat(new File(entry.getName()));
    }

    private Resource getTemplateResource(Collection<Statement> statements) {
        return RdfUtils.find(statements, PluginTemplate.TYPE);
    }

    private Collection<Statement> createDefinitionForDialogs(
            Resource resource, Collection<String> dialogs) {
        List<Statement> output = new ArrayList<>(dialogs.size());
        for (String dialog : dialogs) {
            IRI dialogResource = valueFactory.createIRI(
                    resource.stringValue(), "/dialog/" + dialog);
            output.add(valueFactory.createStatement(
                    resource,
                    valueFactory.createIRI(
                            "http://linkedpipes.com/ontology/dialog"),
                    dialogResource,
                    resource
            ));
            output.add(valueFactory.createStatement(
                    dialogResource,
                    RDF.TYPE,
                    valueFactory.createIRI(
                            "http://linkedpipes.com/ontology/Dialog"),
                    resource
            ));
            output.add(valueFactory.createStatement(
                    dialogResource,
                    valueFactory.createIRI(
                            "http://linkedpipes.com/ontology/name"),
                    valueFactory.createLiteral(dialog),
                    resource
            ));
        }
        return output;
    }

}
