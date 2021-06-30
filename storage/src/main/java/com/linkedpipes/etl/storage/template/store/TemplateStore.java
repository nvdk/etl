package com.linkedpipes.etl.storage.template.store;

import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.List;

/**
 * The identifiers are not IRIs but for example: jar-e-textHolder-0.0.0.jar
 * or 1622805775578-ff2aa41d-16d7-413f-b44b-99b8902a76ad .
 */
public interface TemplateStore {

    String getName();

    List<String> getPluginIdentifiers() throws StoreException;

    List<String> getReferenceIdentifiers() throws StoreException;

    String reserveIdentifier() throws StoreException;

    List<Statement> getPluginInterface(String id)
            throws StoreException;

    void setPluginInterface(
            String id, Collection<Statement> statements)
            throws StoreException;

    List<Statement> getReferenceInterface(String id)
            throws StoreException;

    void setReferenceInterface(
            String id, Collection<Statement> statements)
            throws StoreException;

    List<Statement> getPluginDefinition(String id)
            throws StoreException;

    void setPluginDefinition(
            String id, Collection<Statement> statements)
            throws StoreException;

    Collection<Statement> getReferenceDefinition(String id)
            throws StoreException;

    void setReferenceDefinition(
            String id, Collection<Statement> statements)
            throws StoreException;

    List<Statement> getPluginConfiguration(String id)
            throws StoreException;

    void setPluginConfiguration(
            String id, Collection<Statement> statements)
            throws StoreException;

    List<Statement> getReferenceConfiguration(String id)
            throws StoreException;

    void setReferenceConfiguration(
            String id, Collection<Statement> statements)
            throws StoreException;

    List<Statement> getPluginConfigurationDescription(String id)
            throws StoreException;

    void setPluginConfigurationDescription(
            String id, Collection<Statement> statements)
            throws StoreException;

    byte[] getPluginFile(
            String id, String path)
            throws StoreException;

    void setPluginFile(String id, String path, byte[] content)
            throws StoreException;

    void removePlugin(String id) throws StoreException;

    void removeReference(String id) throws StoreException;

}
