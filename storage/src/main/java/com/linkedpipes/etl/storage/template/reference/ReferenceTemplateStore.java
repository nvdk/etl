package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

import java.util.Collection;
import java.util.Optional;

public interface ReferenceTemplateStore {

    Collection<String> listReferencesTemplate()
            throws StorageException;

    String reserveReferenceIri() throws StorageException;

    Optional<ReferenceTemplate> loadReferenceTemplate(String iri)
            throws StorageException;

    void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException;

    void deleteReferenceTemplate(String iri)
            throws StorageException;

}
