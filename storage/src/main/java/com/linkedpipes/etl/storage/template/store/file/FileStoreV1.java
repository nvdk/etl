package com.linkedpipes.etl.storage.template.store.file;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persist all given data to the given path. Similar to LegacyStore but
 * does utilize only definition and not the interface.
 */
public class FileStoreV1 implements TemplateStore {

    public static final String STORE_NAME = "file-v1";

    private static final int REFERENCE_CREATE_ATTEMPT = 32;

    private static final String DEFINITION = "definition";

    private static final String CONFIGURATION = "configuration";

    private static final String CONFIGURATION_DESCRIPTION =
            "configuration-description";

    private final File directory;

    public FileStoreV1(File directory) {
        this.directory = directory;
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public List<String> getReferenceIdentifiers() {
        return listDirectories().stream()
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.startsWith("jar-"))
                .collect(Collectors.toList());
    }

    protected List<File> listDirectories() {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(files);
    }

    @Override
    public String reserveIdentifier() throws StoreException {
        for (int i = 0; i < REFERENCE_CREATE_ATTEMPT; ++i) {
            String id = createId();
            File path = getDirectory(id);
            if (path.mkdir()) {
                return id;
            }
        }
        throw new StoreException("Can not create directory in: {}", directory);
    }

    protected String createId() {
        return (new Date()).getTime() + "-" + UUID.randomUUID();
    }

    protected File getDirectory(String id) {
        return new File(directory, id);
    }

    @Override
    public List<Statement> getPluginDefinition(String id) throws StoreException {
        return readStatements(id, DEFINITION);
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
        saveStatements(
                id, DEFINITION, definition);
        saveStatements(
                id, CONFIGURATION, configuration);
        saveStatements(
                id, CONFIGURATION_DESCRIPTION, configurationDescription);
    }

    protected void saveStatements(
            String id, String fileName, Collection<Statement> statements)
            throws StoreException {
        File directory = getDirectory(id);
        directory.mkdirs();
        File file = new File(directory, fileName + ".trig");
        try (OutputStream stream = new FileOutputStream(file)) {
            Rio.write(statements, stream, RDFFormat.TRIG);
        } catch (IOException | RuntimeException ex) {
            throw new StoreException("Can't write file.", ex);
        }
    }

    @Override
    public List<Statement> getReferenceDefinition(String id)
            throws StoreException {
        return readStatements(id, DEFINITION);
    }

    @Override
    public void setReferenceDefinition(
            String id, Collection<Statement> statements)
            throws StoreException {
        saveStatements(id, DEFINITION, statements);
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
        saveStatements(id, CONFIGURATION, statements);
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
        Path pathToFile = getFilePath(id, path);
        pathToFile.getParent().toFile().mkdirs();
        try {
            Files.write(pathToFile, content);
        } catch (IOException ex) {
            throw new StoreException("Can't read file.", ex);
        }
    }

    @Override
    public void removeReference(String id) throws StoreException {
        File dir = getDirectory(id);
        if (!FileUtils.deleteQuietly(dir)) {
            throw new StoreException("Can't delete directory with template");
        }
    }

}
