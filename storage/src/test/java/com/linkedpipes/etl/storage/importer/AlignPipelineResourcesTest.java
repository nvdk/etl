package com.linkedpipes.etl.storage.importer;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlignPipelineResourcesTest {

    private final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    @Test
    public void align001() {
        var statements = TestUtils.statements(
                "importer/pipeline-001.trig").selector();
        var pipelines = PipelineAdapter.asPipeline(statements);
        Assertions.assertEquals(1, pipelines.size());
        var pipeline = pipelines.get(0);
        pipeline = new Pipeline(
                valueFactory.createIRI(
                        "http://localhost/resources/pipelines/0000"),
                pipeline.label(), pipeline.version(), pipeline.note(),
                pipeline.tags(), pipeline.executionProfile(),
                pipeline.components(), pipeline.connections());
        var actual  = PipelineAdapter.asRdf(
                AlignPipelineResources.apply(pipeline));
        var expected = TestUtils.statements(
                "importer/pipeline-001-align.trig");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

}
