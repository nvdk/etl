package com.linkedpipes.etl.storage.pipeline.model;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Pipeline(
        Resource resource,
        Literal label,
        Literal version,
        Literal note,
        List<Literal> tags,
        PipelineExecutionProfile executionProfile,
        List<PipelineComponent> components,
        List<PipelineConnection> connections
) {

    public Pipeline(Pipeline other) {
        this(
                other.resource,
                other.label,
                other.version,
                other.note,
                other.tags,
                other.executionProfile,
                other.components,
                other.connections
        );
    }

    public List<Literal> tags() {
        return Collections.unmodifiableList(tags);
    }

    public List<PipelineComponent> components() {
        return Collections.unmodifiableList(components);
    }

    public List<PipelineConnection> connections() {
        return Collections.unmodifiableList(connections);
    }

}
