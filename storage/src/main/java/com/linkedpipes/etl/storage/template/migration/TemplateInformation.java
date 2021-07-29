package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;

/**
 * Define interfaces used by migration.
 */
public class TemplateInformation {

    private TemplateInformation() {
    }

    @FunctionalInterface
    public interface RootSource {

        String getRoot(String iri) throws BaseException;

    }

    @FunctionalInterface
    public interface MappingSource {

        String getMapping(String iri) throws BaseException;

    }

}
