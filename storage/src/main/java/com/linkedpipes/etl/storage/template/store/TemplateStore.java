package com.linkedpipes.etl.storage.template.store;

import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.List;

/**
 * All statements must be in a correct graph.
 */
public interface TemplateStore {

    String COMPONENT_IRI_SUFFIX = "/resources/components/";

    String getName();

    List<String> getReferencesIri() throws StoreException;

    /**
     * Return new IRI created by suffixing given domain name with
     * {@link #COMPONENT_IRI_SUFFIX} and a random suffix similar to GUID.
     */
    String reserveIri(String domain) throws StoreException;

    List<Statement> getPluginDefinition(String iri)
            throws StoreException;

    void setPlugin(
            String iri,
            Collection<Statement> definition,
            Collection<Statement> configuration,
            Collection<Statement> configurationDescription)
            throws StoreException;

    /**
     * Stores may provide custom implementation of this operation.
     */
    default void setReference(
            String iri,
            Collection<Statement> statements,
            Collection<Statement> configuration
    ) throws StoreException {
        try {
            setReferenceDefinition(iri, statements);
            setReferenceConfiguration(iri, configuration);
        } catch (StoreException ex) {
            removeReference(iri);
            throw ex;
        }
    }

    List<Statement> getReferenceDefinition(String iri)
            throws StoreException;

    void setReferenceDefinition(
            String iri, Collection<Statement> definition)
            throws StoreException;

    List<Statement> getPluginConfiguration(String iri)
            throws StoreException;

    List<Statement> getReferenceConfiguration(String iri)
            throws StoreException;

    void setReferenceConfiguration(
            String iri, Collection<Statement> statements)
            throws StoreException;

    List<Statement> getPluginConfigurationDescription(String iri)
            throws StoreException;

    byte[] getPluginFile(
            String iri, String path)
            throws StoreException;

    void setPluginFile(String iri, String path, byte[] content)
            throws StoreException;

    default void silentRemoveReference(String iri) {
        try {
            removeReference(iri);
        } catch(StoreException ex) {
            // Ignore the exception.
        }
    }

    void removeReference(String iri) throws StoreException;

}
