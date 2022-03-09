package com.linkedpipes.etl.storage.rdf;

import com.linkedpipes.etl.storage.TestUtils;
import org.junit.jupiter.api.Test;

public class UpdateResourcesTest {

    @Test
    public void update000() {
        var statements = TestUtils.statements("rdf/update-resource.trig")
                .selector();
        var input = statements.selectByGraph("http://input/000");
        var worker = new UpdateResources("http://new/");
        var actual = worker.update(input);
        var expected = statements.selectByGraph("http://expected/000");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

}
