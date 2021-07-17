package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.utils.Statements;
import org.junit.jupiter.api.Test;

public class ReferenceTemplateFactoryTest {

    @Test
    public void template000() throws Exception {

        String directory = "template/reference/template-0.0.0/";
        var configuration = TestUtils.rdfFromResource(
                directory + "configuration.trig");
        var definition = TestUtils.rdfFromResource(
                directory + "definition.trig");

        var factory = new ReferenceContainerFactory();

        var container = factory.create(
                "202004231840", "http://etl.linkedpipes.com/202004231840",
                definition, configuration);

        var expected = Statements.wrap(
                TestUtils.rdfFromResource(
                        directory + "expected.trig"));

        Statements actual = Statements.arrayList();
        actual.addAll(container.definitionStatements);
        actual.addAll(container.configurationStatements);
        TestUtils.assertIsomorphic(expected, actual);

    }

}

