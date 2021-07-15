package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.template.store.StatementsStore;
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

        var store = new StatementsStore();
        var factory = new ReferenceTemplateFactory(store);

        factory.create(definition, configuration,
                "202004231840", "http://etl.linkedpipes.com/202004231840");

        var expected = Statements.wrap(
                TestUtils.rdfFromResource(
                        directory + "expected.trig"));

        TestUtils.assertIsomorphic(expected, store.getStatements());

    }

}
