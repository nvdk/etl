package com.linkedpipes.etl.storage.pipeline.model;

import java.util.List;

public record PipelineList(
        List<PipelineListItem> pipelines
) {

}
