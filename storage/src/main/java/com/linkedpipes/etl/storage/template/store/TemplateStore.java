package com.linkedpipes.etl.storage.template.store;

import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.List;

public interface TemplateStore {

    String getName();

    List<RepositoryReference> getReferences() throws StoreException;

    String reserveIdentifier() throws StoreException;

    Collection<Statement> getInterface(RepositoryReference reference)
            throws StoreException;

    void setInterface(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException;

    Collection<Statement> getDefinition(RepositoryReference reference)
            throws StoreException;

    void setDefinition(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException;

    Collection<Statement> getConfig(RepositoryReference reference)
            throws StoreException;

    void setConfig(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException;

    Collection<Statement> getConfigDescription(RepositoryReference reference)
            throws StoreException;

    void setConfigDescription(
            RepositoryReference reference, Collection<Statement> statements)
            throws StoreException;

    byte[] getFile(
            RepositoryReference reference, String path)
            throws StoreException;

    void setFile(RepositoryReference reference, String path, byte[] content)
            throws StoreException;

    void remove(RepositoryReference reference) throws StoreException;

}
