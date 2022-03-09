package com.linkedpipes.etl.storage.pipeline.model;

import org.eclipse.rdf4j.model.Resource;

public abstract class PipelineConnection {

    private final Resource resource;

    private final Resource source;

    private final Resource target;

    protected PipelineConnection(
            Resource resource,
            Resource source,
            Resource target
    ) {
        this.resource = resource;
        this.source = source;
        this.target = target;
    }

    public Resource resource() {
        return resource;
    }

    public Resource source() {
        return source;
    }

    public Resource target() {
        return target;
    }

}
