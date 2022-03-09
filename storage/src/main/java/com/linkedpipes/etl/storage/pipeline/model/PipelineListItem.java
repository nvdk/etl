package com.linkedpipes.etl.storage.pipeline.model;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a pipeline in a pipeline list.
 */
public record PipelineListItem(
        Resource resource,
        Literal label,
        List<Literal> tags
) {

}
