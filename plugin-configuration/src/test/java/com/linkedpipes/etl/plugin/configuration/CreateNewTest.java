package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

public class CreateNewTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void createNewFromJarFile() throws Exception {
        var data = TestUtils.statementsFromResource("createNew.trig");
        var actual = ConfigurationFacade.createNewFromJarFile(
                TestUtils.selectGraph(data, "http://input"),
                ConfigurationFacade.loadDescription(
                    TestUtils.selectGraph(data, "http://description")),
                "http://base",
                valueFactory.createIRI("http://expected/jar")
        );
        var expected = TestUtils.selectGraph(data, "http://expected/jar");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void createNewFromTemplate() throws Exception {
        var data = TestUtils.statementsFromResource("createNew.trig");
        var actual = ConfigurationFacade.createNewFromTemplate(
                TestUtils.selectGraph(data, "http://input"),
                ConfigurationFacade.loadDescription(
                    TestUtils.selectGraph(data, "http://description")),
                "http://base",
                valueFactory.createIRI("http://expected/template")
        );
        var expected = TestUtils.selectGraph(data, "http://expected/template");
        TestUtils.assertIsomorphic(actual, expected);
    }

}
