package com.linkedpipes.etl.storage.importer;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

import java.util.Collections;
import java.util.Map;

public record ImportOptions(
        Map<Resource, PipelineOptions> pipelines,
        /*
         * If true new templates are imported.
         */
        boolean importNewTemplates,
        /*
         * If true local templates can be changed as a result of import.
         * Used only if {@link #importTemplates} is set to true.
         */
        boolean updateLocalTemplates
) {

    public record PipelineOptions(
            /*
             * Identification of a pipeline these settings apply to.
             */
            Resource pipeline,
            /*
             * User can specify suffix for pipeline resource.
             */
            String suffix,
            /*
             * Label to set to given pipeline.
             */
            @Deprecated
            Literal label,
            /*
             * If true resources will be changed aligned with the
             * pipeline resources. If false only the pipeline resource,
             * can be changed.
             */
            boolean updateResources
    ) {

    }

    public static ImportOptions defaultOptions() {
        return new ImportOptions(Collections.emptyMap(), true, false);
    }

}
