package com.linkedpipes.etl.storage.template.reference.migration;

import com.linkedpipes.etl.storage.StorageException;

@FunctionalInterface
public interface PluginTemplateSource {

    /**
     * Given a template IRI returns the plugin template IRI. If the
     * template is already plugin template return the template IRI.
     * Return null if the plugin template does not exist.
     */
    String getPluginTemplate(String iri) throws StorageException;

}
