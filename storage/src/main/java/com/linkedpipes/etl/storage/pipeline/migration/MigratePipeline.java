package com.linkedpipes.etl.storage.pipeline.migration;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.template.reference.migration.PluginTemplateSource;

public class MigratePipeline {

    private final PluginTemplateSource templateSource;

    public MigratePipeline(PluginTemplateSource templateSource) {
        this.templateSource = templateSource;
    }

    /**
     * The migration works on statements, so no pipeline definition
     * is returned from this function.
     */
    public Pipeline migratePipeline(Pipeline pipeline) throws StorageException {
        int version = 0;
        if (pipeline.version() != null) {
            version = pipeline.version().intValue();
        }
        if (version < 1) {
            pipeline = (new PipelineV0()).migrateToV1(pipeline);
        }
        if (version < 2) {
            pipeline = (new PipelineV1(templateSource)).migrateToV2(pipeline);
        }
        if (version < 5) {
            pipeline = (new PipelineV4()).migrateToV5(pipeline);
        }
        return pipeline;
    }

}
