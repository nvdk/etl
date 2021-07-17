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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LegacyStore implements TemplateStore {

    public static final String STORE_NAME = "legacy";

    private static final int REFERENCE_CREATE_ATTEMPT = 32;

    private static final String INTERFACE = "interface";

    private static final String DEFINITION = "definition";

    private static final String CONFIGURATION = "configuration";

    private static final String CONFIGURATION_DESCRIPTION =
            "configuration-description";

    private final File directory;

    private final Base64.Encoder encoder = Base64.getEncoder();

    public LegacyStore(File directory) {
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
    public List<String> getReferenceIdentifiers() {
        return listDirectories().stream()
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.startsWith("jar-"))
                .collect(Collectors.toList());
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
    public List<Statement> getPluginInterface(String id)
            throws StoreException {
        return readStatements(id, INTERFACE);
    }

    protected List<Statement> readStatements(String id, String fileName)
            throws StoreException {
        File file = new File(getDirectory(id), fileName + ".trig");
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
                id, INTERFACE, definition);
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
    public List<Statement> getReferenceInterface(String id)
            throws StoreException {
        return readStatements(id, INTERFACE);
    }

    @Override
    public void setReferenceInterface(
            String id, Collection<Statement> statements)
            throws StoreException {
        saveStatements(id, INTERFACE, statements);
    }

    @Override
    public List<Statement> getPluginDefinition(String id)
            throws StoreException {
        return readStatements(id, DEFINITION);
    }

    @Override
    public Collection<Statement> getReferenceDefinition(String id)
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
