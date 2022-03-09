package com.linkedpipes.etl.storage.pipeline.adapter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.pipeline.migration.MigratePipeline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PipelineAdapterTest {

    private static final Map<String, String> TEMPLATE_TO_PLUGIN =
            new HashMap<>();

    static {
        String prefix = "http://etl.linkedpipes.com/resources/components/";
        TEMPLATE_TO_PLUGIN.put(
                prefix + "e-httpGetFile/0.0.0",
                prefix + "e-httpGetFile/0.0.0");
        TEMPLATE_TO_PLUGIN.put(
                prefix + "t-excelToCsv/0.0.0",
                prefix + "t-excelToCsv/0.0.0");
        TEMPLATE_TO_PLUGIN.put(
                "http://localhost:8080/resources/components/1621865731462",
                prefix + "t-sparqlConstructChunked/0.0.0");
    }

    @Test
    public void loadPipelineV0() throws StorageException {
        var statements = TestUtils.statements(
                "migration/v0/pipelines/created-1472483376815.trig")
                .selector();
        MigratePipeline worker = new MigratePipeline(TEMPLATE_TO_PLUGIN::get);
        var pipelines = PipelineAdapter.asPipeline(statements);
        Assertions.assertEquals(1, pipelines.size());
        var pipeline = worker.migratePipeline(pipelines.get(0));
        var actual = PipelineAdapter.asRdf(pipeline);
        var expected = TestUtils.statements(
                "migration/v0/expected-pipelines.trig");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

    @Test
    public void loadPipelineV1() throws StorageException {
        var statements = TestUtils.statements(
                "migration/v1/pipelines/created-1621865694389.trig")
                .selector();
        MigratePipeline worker = new MigratePipeline(TEMPLATE_TO_PLUGIN::get);
        var pipelines = PipelineAdapter.asPipeline(statements);
        Assertions.assertEquals(1, pipelines.size());
        var pipeline = worker.migratePipeline(pipelines.get(0));
        var actual = PipelineAdapter.asRdf(pipeline);
        var expected = TestUtils.statements(
                "migration/v1/expected-pipelines.trig");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

    @Test
    public void loadPipelineV2() throws StorageException {
        var statements = TestUtils.statements(
                "migration/v2/pipelines/created-1621874367769.trig")
                .selector();
        MigratePipeline worker = new MigratePipeline(TEMPLATE_TO_PLUGIN::get);
        var pipelines = PipelineAdapter.asPipeline(statements);
        Assertions.assertEquals(1, pipelines.size());
        var pipeline = worker.migratePipeline(pipelines.get(0));
        var actual = PipelineAdapter.asRdf(pipeline);
        var expected = TestUtils.statements(
                "migration/v2/expected-pipelines.trig");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

    @Test
    public void loadPipelineV3() throws StorageException {
        var statements = TestUtils.statements(
                "migration/v3/pipelines/1621880827570.trig")
                .selector();
        MigratePipeline worker = new MigratePipeline(TEMPLATE_TO_PLUGIN::get);
        var pipelines = PipelineAdapter.asPipeline(statements);
        Assertions.assertEquals(1, pipelines.size());
        var pipeline = worker.migratePipeline(pipelines.get(0));
        var actual = PipelineAdapter.asRdf(pipeline);
        var expected = TestUtils.statements(
                "migration/v3/expected-pipelines.trig");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

    @Test
    public void loadPipelineV4() throws StorageException {
        var statements = TestUtils.statements(
                "migration/v4/pipelines/1621937390630.trig")
                .selector();
        MigratePipeline worker = new MigratePipeline(TEMPLATE_TO_PLUGIN::get);
        var pipelines = PipelineAdapter.asPipeline(statements);
        Assertions.assertEquals(1, pipelines.size());
        var pipeline = worker.migratePipeline(pipelines.get(0));
        var actual = PipelineAdapter.asRdf(pipeline);
        var expected = TestUtils.statements(
                "migration/v4/expected-pipelines.trig");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

}
