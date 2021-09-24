package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;

/**
 * Define interfaces used by migration.
 */
public class MigrationSources {

    private MigrationSources() {
    }

    @FunctionalInterface
    public interface RootSource {

        /**
         * For given reference template returns the IRI of the root template,
         * i.e. plugin template.
         */
        String getRoot(String iri) throws BaseException;

    }

    @FunctionalInterface
    public interface MappingSource {

        /**
         * For given reference template returns value of remote mapping, i.e.
         * how the template is known on other instances.
         */
        String getMapping(String iri) throws BaseException;

    }

}
