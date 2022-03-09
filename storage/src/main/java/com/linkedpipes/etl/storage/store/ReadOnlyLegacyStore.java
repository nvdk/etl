package com.linkedpipes.etl.storage.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.rdf.StatementsFile;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This store is read only as it should not be used by the current
 * version. It is there only to support migration from older versions.
 * <p>
 * Unlike other stores the legacy store does not utilize IRI but rather
 * an internal identification.
 */
public class ReadOnlyLegacyStore implements Store {

    public static final String STORE_NAME = "legacy";

    private static final String INTERFACE = "interface";

    private static final String DEFINITION = "definition";

    private static final String CONFIGURATION = "configuration";

    private static final String PIPELINE_DIR = "pipelines";

    private static final String TEMPLATES_DIR = "templates";

    protected final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    /**
     * Storage directory.
     */
    protected final File storageDirectory;

    protected final Map<String, Resource> knownAsMap = new HashMap<>();

    protected final String baseUrl;

    protected Integer templateVersion;

    public ReadOnlyLegacyStore(
            File storeDirectory, String baseUrl, int templateVersion) {
        this.storageDirectory = storeDirectory;
        this.baseUrl = baseUrl;
        this.templateVersion = templateVersion;
    }

    @Override
    public void initialize() throws StorageException {
        buildKnownAsMap();
    }

    /**
     * Before version 5 the mapping is stored in an extra file.
     */
    protected void buildKnownAsMap() throws StorageException {
        File mappingFile = new File(storageDirectory, "knowledge/mapping.trig");
        if (!mappingFile.exists()) {
            return;
        }
        StatementsFile content = Statements.arrayList().file();
        try {
            content.addAll(mappingFile);
        } catch (IOException ex) {
            throw new StorageException("Can't read mapping file.", ex);
        }
        content.stream().filter(st -> st.getPredicate().equals(OWL.SAMEAS))
                .forEach(st -> {
                    Resource remote = st.getSubject();
                    String local = st.getObject().stringValue();
                    knownAsMap.put(local, remote);
                });
    }

    @Override
    public String toString() {
        return getName() + "-v" + templateVersion;
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public Collection<String> listPluginTemplates() {
        throw new UnsupportedOperationException(
                "This store does not support plugins.");
    }

    @Override
    public Optional<PluginTemplate> loadPluginTemplate(String iri) {
        throw new UnsupportedOperationException(
                "This store does not support plugins.");
    }

    @Override
    public void storePluginTemplate(PluginTemplate template) {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public Collection<String> listReferencesTemplate() {
        String prefix = baseUrl + "/resources/components/";
        return listDirectories(new File(storageDirectory, TEMPLATES_DIR))
                .stream()
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.startsWith("jar-"))
                .map(name -> prefix + name)
                .collect(Collectors.toList());
    }

    protected List<File> listDirectories(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(files);
    }

    @Override
    public String reserveReferenceIri() {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public Optional<ReferenceTemplate> loadReferenceTemplate(String iri)
            throws StorageException {
        Set<Statement> statements = new HashSet<>();
        statements.addAll(readTemplateStatements(iri, DEFINITION));
        statements.addAll(readTemplateStatements(iri, INTERFACE));
        statements.addAll(readTemplateStatements(iri, CONFIGURATION));
        List<ReferenceTemplate> templates =
                ReferenceTemplateAdapter.asReferenceTemplates(
                        Statements.wrap(statements));
        if (templates.size() != 1) {
            throw new StorageException(
                    "Found " + templates.size() + " templates " +
                            "for " + iri + " .");
        }
        ReferenceTemplate result = templates.get(0);
        String resourceAsStr = result.resource().stringValue();
        return Optional.of(new ReferenceTemplate(
                result.resource(), result.template(),
                result.prefLabel(), result.description(), result.note(),
                result.color(), result.tags(),
                knownAsMap.getOrDefault(resourceAsStr, result.knownAs()),
                result.pluginTemplate(),
                valueFactory.createLiteral(templateVersion),
                result.configuration(), result.configurationGraph()));
    }

    protected Statements readTemplateStatements(
            String iri, String fileName) throws StorageException {
        File file = new File(getTemplateDirectory(iri), fileName + ".trig");
        return readStatements(file);
    }

    protected Statements readStatements(File file) throws StorageException {
        StatementsFile statements = Statements.arrayList().file();
        try {
            statements.addAllIfExists(file);
        } catch (IOException ex) {
            throw new StorageException("Cant read file.", ex);
        }
        return statements;
    }

    protected File getTemplateDirectory(String id) {
        if (id.contains("://")) {
            // It is full IRI identification, we remove all but the last part.
            id = id.substring(id.lastIndexOf("/"));
        }
        return new File(storageDirectory, "templates/" + id);
    }

    @Override
    public void storeReferenceTemplate(ReferenceTemplate template) {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public void deleteReferenceTemplate(String iri) {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public String basePipelineUrl() {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public List<String> listPipelines() {
        return listDirectories(new File(storageDirectory, PIPELINE_DIR))
                .stream()
                .filter(File::isFile)
                .map(File::getName)
                .filter(name -> !name.endsWith(".backup"))
                .map(name -> name.substring(
                        0, name.length() - ".trig".length()))
                .collect(Collectors.toList());
    }

    @Override
    public String reservePipelineIri(String suffix) {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public Optional<Pipeline> loadPipeline(String iri) throws StorageException {
        File directory = new File(storageDirectory, PIPELINE_DIR);
        File file = new File(directory, iri + ".trig");
        StatementsSelector statements = readStatements(file).selector();
        List<Pipeline> result = PipelineAdapter.asPipeline(statements);
        if (result.size() != 1) {
            throw new StorageException(
                    "Invalid pipeline '" + file + "' file, "
                            + result.size() + " pipelines found.");
        }
        return Optional.of(result.get(0));
    }

    @Override
    public void storePipeline(Pipeline pipeline) {
        throw new UnsupportedOperationException("This store is read only.");
    }

    @Override
    public void deletePipeline(String iri) {
        throw new UnsupportedOperationException("This store is read only.");
    }

}
