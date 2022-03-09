package com.linkedpipes.etl.storage.store;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsFile;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.template.plugin.adapter.PluginTemplateAdapter;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Store data using RDF format in given directory.
 */
public class RdfDirectoryStoreV1 implements Store {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfDirectoryStoreV1.class);

    public static final String STORE_NAME = "rdf-directory-v1";

    protected static final String PLUGIN_DIRECTORY = "plugin";

    protected static final String REFERENCE_DIRECTORY = "reference";

    protected static final String PIPELINE_DIRECTORY = "pipeline";

    protected static final String PIPELINE_PREFIX = "/pipelines/";

    protected static final String TEMPLATE_PREFIX = "/components/";

    /**
     * Prefix used by core plugins.
     */
    protected static final String PLUGIN_PREFIX =
            "http://etl.linkedpipes.com/resources/components/";

    protected final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    protected final Object lock = new Object();

    protected final File storeDirectory;

    protected final String baseUrl;

    public RdfDirectoryStoreV1(File storeDirectory, String baseUrl) {
        this.storeDirectory = storeDirectory;
        this.baseUrl = baseUrl + "/resources";
        createDirectories();
    }

    protected void createDirectories() {
        pipelineDirectory().mkdirs();
        referenceDirectory().mkdirs();
        pluginDirectory().mkdirs();
    }

    protected File pipelineDirectory() {
        return new File(storeDirectory, PIPELINE_DIRECTORY);
    }

    protected File referenceDirectory() {
        return new File(storeDirectory, REFERENCE_DIRECTORY);
    }

    protected File pluginDirectory() {
        return new File(storeDirectory, PLUGIN_DIRECTORY);
    }

    @Override
    public String basePipelineUrl() {
        return baseUrl + "/resources/pipelines/";
    }

    @Override
    public Collection<String> listPipelines() {
        return listDirectories(pipelineDirectory()).stream()
                .map(File::getName)
                .filter(name -> name.endsWith(".trig"))
                .map(name -> fileNameToIri(pipelinePrefix(), name))
                .toList();
    }

    private String pipelinePrefix() {
        return baseUrl + PIPELINE_PREFIX;
    }

    protected List<File> listDirectories(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(files);
    }

    protected String fileNameToIri(String baseUrl, String content) {
        String replaced = content.replaceAll("&", "%").replace(".trig", "");
        String decoded = URLDecoder.decode(replaced, StandardCharsets.UTF_8);
        if (decoded.contains("://")) {
            return decoded;
        }
        return baseUrl + decoded;
    }

    @Override
    public String reservePipelineIri(String suffix) {
        return pipelinePrefix() + (suffix == null ? createId() : suffix);
    }

    private String createId() {
        synchronized (lock) {
            return (new Date()).getTime() + "-" + UUID.randomUUID();
        }
    }

    @Override
    public Optional<Pipeline> loadPipeline(String iri) throws StorageException {
        if (iri == null) {
            return Optional.empty();
        }
        File file = fileInDirectory(PIPELINE_DIRECTORY, pipelinePrefix(), iri);
        if (!file.exists()) {
            return Optional.empty();
        }
        StatementsSelector statements = readStatements(file).selector();
        List<Pipeline> pipelines = PipelineAdapter.asPipeline(statements);
        if (pipelines.size() != 1) {
            throw new StorageException(
                    "Invalid pipeline file: '" + file + "'.");

        }
        return Optional.of(pipelines.get(0));
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

    protected File fileInDirectory(String directory, String prefix, String iri) {
        String fileName = iriToFileName(prefix, iri) + ".trig";
        return new File(storeDirectory, directory + File.separator + fileName);
    }

    protected String iriToFileName(String prefix, String content) {
        content = content.replace(prefix, "");
        String encoded = URLEncoder.encode(content, StandardCharsets.UTF_8);
        return encoded.replaceAll("%", "&");
    }

    @Override
    public void storePipeline(Pipeline pipeline) throws StorageException {
        if (pipeline.resource() == null) {
            throw new StorageException("Pipeline resource can not be null.");
        }
        File file = fileInDirectory(
                PIPELINE_DIRECTORY, pipelinePrefix(), pipeline.resource());
        Statements statements = PipelineAdapter.asRdf(pipeline);
        writeStatements(file, statements.file());
    }

    public File fileInDirectory(
            String directory, String prefix, Resource resource) {
        return fileInDirectory(directory, prefix, resource.stringValue());
    }

    protected void writeStatements(File file, StatementsFile statements)
            throws StorageException {
        File tempFile = new File(file + ".swp");
        try {
            statements.writeToFile(tempFile, RDFFormat.TRIG);
        } catch (IOException ex) {
            throw new StorageException("Can't write to file.", ex);
        }
        try {
            Files.move(
                    tempFile.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            tempFile.delete();
            throw new StorageException("Can't update file.", ex);
        }
    }

    @Override
    public void deletePipeline(String iri) {
        File file = fileInDirectory(PIPELINE_DIRECTORY, pipelinePrefix(), iri);
        tryDeleteFile(file);
    }

    protected void tryDeleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException ex) {
            LOG.warn("Can't delete pipeline file.", ex);
        }
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public Collection<String> listPluginTemplates() {
        return listDirectories(pluginDirectory()).stream()
                .map(File::getName)
                .filter(name -> name.endsWith(".trig"))
                .map(name -> fileNameToIri(pluginPrefix(), name))
                .toList();
    }

    private String pluginPrefix() {
        return PLUGIN_PREFIX;
    }

    @Override
    public Optional<PluginTemplate> loadPluginTemplate(String iri)
            throws StorageException {
        if (iri == null) {
            return Optional.empty();
        }
        File file = fileInDirectory(PLUGIN_DIRECTORY, pluginPrefix(), iri);
        if (!file.exists()) {
            return Optional.empty();
        }
        Statements statements = readStatements(file);
        List<PluginTemplate> templates =
                PluginTemplateAdapter.asPluginTemplates(statements);
        if (templates.size() != 1) {
            throw new StorageException(
                    "Invalid plugin template file: '" + file + "'.");

        }
        return Optional.of(templates.get(0));
    }

    @Override
    public void storePluginTemplate(PluginTemplate template)
            throws StorageException {
        if (template.resource() == null) {
            throw new StorageException(
                    "Plugin template resource can not be null.");
        }
        File file = fileInDirectory(
                PLUGIN_DIRECTORY, pluginPrefix(), template.resource());
        Statements statements = PluginTemplateAdapter.asStatements(template);
        writeStatements(file, statements.file());
    }

    @Override
    public Collection<String> listReferencesTemplate() {
        return listDirectories(referenceDirectory()).stream()
                .map(File::getName)
                .filter(name -> name.endsWith(".trig"))
                .map(name -> fileNameToIri(templatePrefix(), name))
                .toList();
    }

    protected String templatePrefix() {
        return baseUrl + TEMPLATE_PREFIX;
    }

    @Override
    public String reserveReferenceIri() {
        return templatePrefix() + createId();
    }

    @Override
    public Optional<ReferenceTemplate> loadReferenceTemplate(String iri)
            throws StorageException {
        if (iri == null) {
            return Optional.empty();
        }
        File file = fileInDirectory(REFERENCE_DIRECTORY, templatePrefix(), iri);
        if (!file.exists()) {
            return Optional.empty();
        }
        Statements statements = readStatements(file);
        List<ReferenceTemplate> templates =
                ReferenceTemplateAdapter.asReferenceTemplates(statements);
        if (templates.size() != 1) {
            throw new StorageException(
                    "Invalid reference template file: '" + file + "'.");
        }
        return Optional.of(templates.get(0));
    }

    @Override
    public void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException {
        if (template.resource() == null) {
            throw new StorageException(
                    "Reference template resource can not be null.");
        }
        File file = fileInDirectory(
                REFERENCE_DIRECTORY, templatePrefix(), template.resource());
        Statements statements = ReferenceTemplateAdapter.asRdf(template);
        writeStatements(file, statements.file());
    }

    @Override
    public void deleteReferenceTemplate(String iri) {
        File file = fileInDirectory(
                REFERENCE_DIRECTORY, templatePrefix(), iri);
        tryDeleteFile(file);
    }

}
