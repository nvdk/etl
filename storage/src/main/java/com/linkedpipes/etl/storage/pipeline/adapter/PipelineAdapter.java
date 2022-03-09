package com.linkedpipes.etl.storage.pipeline.adapter;

import com.linkedpipes.etl.storage.pipeline.adapter.rdf.PipelineListToRdf;
import com.linkedpipes.etl.storage.pipeline.adapter.rdf.PipelineToRdf;
import com.linkedpipes.etl.storage.pipeline.adapter.rdf.RdfToPipeline;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineList;
import com.linkedpipes.etl.storage.pipeline.model.PipelineListItem;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.List;

public class PipelineAdapter {

    public static List<Pipeline> asPipeline(StatementsSelector statements) {
        return RdfToPipeline.asPipelines(statements);
    }

    public static PipelineList asPipelineList(List<Pipeline> pipelines) {
        return new PipelineList(pipelines.stream().map(
                pipeline -> new PipelineListItem(
                        pipeline.resource(),
                        pipeline.label(),
                        pipeline.tags())).toList());
    }

    public static Statements asRdf(Pipeline pipeline) {
        return PipelineToRdf.pipelineAsRdf(pipeline);
    }

    public static Statements asRdf(PipelineList pipeline) {
        return PipelineListToRdf.pipelineListAsRdf(pipeline);
    }


}
