package com.linkedpipes.etl.storage.pipeline.migration;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * The last version was pipeline version 2, but to unify the versioning
 * for configuration in the pipeline and in templates we skip all the
 * way to version 5.
 */
public class PipelineV4 {

    public Pipeline migrateToV5(Pipeline pipeline) {
        return new Pipeline(
                pipeline.resource(),
                pipeline.label(),
                SimpleValueFactory.getInstance().createLiteral(5),
                pipeline.note(),
                pipeline.tags(),
                pipeline.executionProfile(),
                pipeline.components(),
                pipeline.connections()
        );
    }
}
