package com.linkedpipes.etl.storage.template.store.legacy;

import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LegacyStore implements TemplateStore {

    public static final String STORE_NAME = "legacy";

    private static final int REFERENCE_CREATE_ATTEMPT = 32;

    private static final Logger LOG =
            LoggerFactory.getLogger(LegacyStore.class);

    protected final File directory;

    public LegacyStore(File directory) {
        this.directory = directory;
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public List<RepositoryReference> getReferences() throws StoreException {
        List<RepositoryReference> output = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return output;
        }
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            if (file.getName().startsWith("jar-")) {
                output.add(RepositoryReference.createJar(file.getName()));
            } else {
                output.add(RepositoryReference.createReference(file.getName()));
            }
        }
        return output;
    }

    @Override
    public String reserveIdentifier() throws StoreException {
        for (int i = 0; i < REFERENCE_CREATE_ATTEMPT; ++i) {
            String id = createId();
            File path = getDirectory(RepositoryReference.createReference(id));
            if (path.mkdir()) {
                return id;
            }
        }
        throw new StoreException("Can not create directory in: {}", directory);
    }

    protected String createId() {
        return (new Date()).getTime() + "-" + UUID.randomUUID();
    }

    @Override
    public Collection<Statement> getInterface(RepositoryReference reference)
            throws StoreException {
        return readStatements(reference, "interface");
    }

    protected Collection<Statement> readStatements(
            RepositoryReference reference, String fileName)
            throws StoreException {
        File file = new File(getDirectory(reference), fileName + ".trig");
        try (InputStream stream = new FileInputStream(file)) {
            return new ArrayList<>(Rio.parse(stream, RDFFormat.TRIG));
        } catch (IOException | RuntimeException ex) {
            throw new StoreException("Can't load file.", ex);
        }
    }

    protected File getDirectory(RepositoryReference ref) {
        return new File(directory, ref.getId());
    }

    @Override
    public void setInterface(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException {
        saveStatements(reference, "interface", statements);
    }

    protected void saveStatements(
            RepositoryReference reference,
            String fileName, Collection<Statement> statements)
            throws StoreException {
        File file = new File(getDirectory(reference), fileName + ".trig");
        file.getParentFile().mkdirs();
        try (OutputStream stream = new FileOutputStream(file)) {
            Rio.write(statements, stream, RDFFormat.TRIG);
        } catch (IOException | RuntimeException ex) {
            throw new StoreException("Can't write file.", ex);
        }
    }

    @Override
    public Collection<Statement> getDefinition(RepositoryReference reference)
            throws StoreException {
        return readStatements(reference, "definition");
    }

    @Override
    public void setDefinition(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException {
        saveStatements(reference, "definition", statements);
    }

    @Override
    public Collection<Statement> getConfig(RepositoryReference reference)
            throws StoreException {
        return readStatements(reference, "configuration");
    }

    @Override
    public void setConfig(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException {
        saveStatements(reference, "configuration", statements);
    }

    @Override
    public Collection<Statement> getConfigDescription(
            RepositoryReference reference) throws StoreException {
        return readStatements(reference, "configuration-description");
    }

    @Override
    public void setConfigDescription(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException {
        saveStatements(reference, "configuration-description", statements);
    }

    @Override
    public byte[] getFile(RepositoryReference reference, String path)
            throws StoreException {
        Path pathToFile = getFilePath(reference, path);
        try {
            return Files.readAllBytes(pathToFile);
        } catch (IOException ex) {
            throw new StoreException("Can't read file.", ex);
        }
    }

    protected Path getFilePath(RepositoryReference reference, String path) {
        if (reference.getType() != Template.Type.JAR_TEMPLATE) {
            return null;
        }
        return (new File(getDirectory(reference), path)).toPath();
    }

    @Override
    public void setFile(
            RepositoryReference reference, String path, byte[] content)
            throws StoreException {
        Path pathToFile = getFilePath(reference, path);
        pathToFile.getParent().toFile().mkdirs();
        try {
            Files.write(pathToFile, content);
        } catch (IOException ex) {
            throw new StoreException("Can't read file.", ex);
        }

    }

    @Override
    public void remove(RepositoryReference reference) throws StoreException {
        File dir = getDirectory(reference);
        if (!FileUtils.deleteQuietly(dir)) {
            LOG.warn("Can not delete: {}", dir);
        }
    }

}
