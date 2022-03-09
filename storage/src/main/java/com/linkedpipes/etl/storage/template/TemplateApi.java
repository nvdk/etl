package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateApi;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateApi;

import java.util.List;
import java.util.Optional;

public interface TemplateApi extends PluginTemplateApi, ReferenceTemplateApi {

    boolean isPluginTemplate(String iri)
            throws StorageException;

    boolean isReferenceTemplate(String iri)
            throws StorageException;

    /**
     * Return parent for given template. If the template of given IRI does
     * not exist or is a plugin template return null.
     */
    Optional<String> getParent(String iri)
            throws StorageException;

    /**
     * The root template is the first one in the list. If this is called
     * on a plugin template the returned array contains only the given IRI.
     */
    List<String> getAncestors(String iri)
            throws StorageException;

    List<String> getSuccessors(String iri)
            throws StorageException;

    /**
     * Return configuration of given component after all parents
     * configurations has been applied.
     */
    Optional<Statements> getEffectiveConfiguration(String iri)
            throws StorageException;

    /**
     * Return configuration that should be used for instances
     * of given template.
     */
    Optional<Statements> getNewConfiguration(String iri)
            throws StorageException;



}
