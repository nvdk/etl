package com.linkedpipes.etl.storage.rdf;

import com.linkedpipes.etl.storage.TestUtils;
import org.junit.jupiter.api.Test;

public class ReplacePrefixTest {

    @Test
    public void replace000() {
        var statements = TestUtils.statements("rdf/replace-prefix.trig")
                .selector();
        var input = statements.selectByGraph("http://input/000");
        var worker = new ReplacePrefix("http://old/", "http://new/");
        var actual = worker.replace(input);
        var expected = statements.selectByGraph("http://expected/000");
        TestUtils.assertIsomorphicIgnoreGraph(expected, actual);
    }

}
