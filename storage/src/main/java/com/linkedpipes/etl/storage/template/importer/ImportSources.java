package com.linkedpipes.etl.storage.template.importer;

import com.linkedpipes.etl.storage.BaseException;

/**
 * Interfaces used to get access to information needed to import templates.
 */
public class ImportSources {

    private ImportSources() {
    }

    @FunctionalInterface
    public interface PluginSource {

        /**
         * Checks if given IRI belongs to local plugin template.
         */
        boolean isPluginTemplate(String iri) throws BaseException;

    }

    @FunctionalInterface
    public interface TemplateSource {

        /**
         * For a given template or its known as property, returns
         * the local IRI.
         */
        String getKnownAs(String iri) throws BaseException;

    }

}