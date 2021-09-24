package com.linkedpipes.etl.storage.template.store.legacy;

import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This store is read only as it should not be used by the current
 * version. It is there only to support migration from older versions.
 *
 * Unlike other stores the legacy store does not utilize IRI but rather
 * an internal identification.
 */
public class ReadOnlyLegacyStore implements TemplateStore {

    public static final String STORE_NAME = "legacy";

    private static final String INTERFACE = "interface";

    private static final String DEFINITION = "definition";

    private static final String CONFIGURATION = "configuration";

    private static final String CONFIGURATION_DESCRIPTION =
            "configuration-description";

    private final File directory;

    public ReadOnlyLegacyStore(File directory) {
        this.directory = directory;
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    protected List<File> listDirectories() {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(files);
    }

    @Override
    public List<String> getReferencesIri() {
        return listDirectories().stream()
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.startsWith("jar-"))
                .collect(Collectors.toList());
    }

    @Override
    public String reserveIri(String domain) throws StoreException {
        throw new StoreException("Operation is not supported");
    }

    protected File getDirectory(String id) {
        return new File(directory, id);
    }

    @Override
    public List<Statement> getPluginDefinition(String id)
            throws StoreException {
        Set<Statement> result = new HashSet<>();
        result.addAll(readStatements(id, DEFINITION));
        result.addAll(readStatements(id, INTERFACE));
        return new ArrayList<>(result);
    }

    protected List<Statement> readStatements(String id, String fileName)
            throws StoreException {
        File file = new File(getDirectory(id), fileName + ".trig");
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try (InputStream stream = new FileInputStream(file)) {
            return new ArrayList<>(Rio.parse(stream, RDFFormat.TRIG));
        } catch (IOException | RuntimeException ex) {
            throw new StoreException("Can't load file.", ex);
        }
    }

    @Override
    public void setPlugin(
            String id,
            Collection<Statement> definition,
            Collection<Statement> configuration,
            Collection<Statement> configurationDescription)
            throws StoreException {
        throw new StoreException("Operation is not supported");
    }

    @Override
    public List<Statement> getReferenceDefinition(String id)
            throws StoreException {
        Set<Statement> result = new HashSet<>();
        result.addAll(readStatements(id, DEFINITION));
        result.addAll(readStatements(id, INTERFACE));
        return new ArrayList<>(result);
    }

    @Override
    public void setReferenceDefinition(
            String id, Collection<Statement> statements)
            throws StoreException {
        throw new StoreException("Operation is not supported");
    }

    @Override
    public List<Statement> getPluginConfiguration(String id)
            throws StoreException {
        return readStatements(id, CONFIGURATION);
    }

    @Override
    public List<Statement> getReferenceConfiguration(
            String id) throws StoreException {
        return readStatements(id, CONFIGURATION);
    }

    @Override
    public void setReferenceConfiguration(
            String id, Collection<Statement> statements)
            throws StoreException {
        throw new StoreException("Operation is not supported");
    }

    @Override
    public List<Statement> getPluginConfigurationDescription(String id)
            throws StoreException {
        return readStatements(id, CONFIGURATION_DESCRIPTION);
    }

    @Override
    public byte[] getPluginFile(String id, String path)
            throws StoreException {
        Path pathToFile = getFilePath(id, path);
        try {
            return Files.readAllBytes(pathToFile);
        } catch (IOException ex) {
            throw new StoreException("Can't read file.", ex);
        }
    }

    protected Path getFilePath(String id, String path) {
        return (new File(getDirectory(id), path)).toPath();
    }

    @Override
    public void setPluginFile(String id, String path, byte[] content)
            throws StoreException {
        throw new StoreException("Operation is not supported");
    }

    @Override
    public void removeReference(String id) throws StoreException {
        throw new StoreException("Operation is not supported");
    }

}
