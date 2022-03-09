package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

import java.util.Collection;
import java.util.Optional;

public interface ReferenceTemplateApi {

    /**
     * Return IRI of all stored plugin templates.
     */
    Collection<String> listReferenceTemplates()
            throws StorageException;

    Optional<ReferenceTemplate> loadReferenceTemplate(String iri)
            throws StorageException;

    /**
     * Update or create a reference template and return its IRI.
     */
    ReferenceTemplate storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException;

    void deleteReferenceTemplate(String iri)
            throws StorageException;

}
