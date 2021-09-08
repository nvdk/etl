package com.linkedpipes.etl.storage.template.importer;

import org.junit.jupiter.api.Test;

public class TestImportTemplates {

    @Test
    public void importPipelineV1() {

    }


    @Test
    public void importPipelineV5() {

    }

    /**
     * This test should not create redundant templates as they should be
     * imported only in the first pipeline import.
     */
    @Test
    public void importPipelineV5Twice() {

    }

}

//    public static ValueFactory valueFactory = SimpleValueFactory.getInstance();
//
//    @Test
//    public void loadPipeline00() throws Exception {
//        var input = TestUtils.statementsFromResource(
//                "pipeline/importer/00-input.trig");
//
//        TemplateStore store = new TemplateMemoryStore("http://localhost");
//        TemplatesData data = new TemplatesData(store);
//        data.templates.put("http://localhost/components/124",
//                Template.Reference(null));
//
//        ImportTemplateOptions options = new ImportTemplateOptions();
//        var actual = createCommand(data, new HashMap<>()).apply(
//                options, Statements.wrap(input),
//                valueFactory.createIRI("http://localhost/pipeline"));
//
//        Set<Resource> actualGraph = new HashSet<>();
//        actual.unUsedStatements.forEach(st -> actualGraph.add(st.getContext()));
//        Assertions.assertIterableEquals(Arrays.asList(
//                valueFactory.createIRI(
//                        "http://localhost/pipeline/3405c1ee/configuration"),
//                valueFactory.createIRI("http://localhost/pipeline")),
//                actualGraph
//        );
//
//        var templateMapping = actual.remoteToLocal;
//
//        Assertions.assertTrue(templateMapping.isEmpty());
//
//    }
//
//    public ImportTemplates createCommand(
//            TemplatesData data, Map<String, String> mapping) {
//        return new ImportTemplates(data) {
//
//            @Override
//            protected Map<String, String> collectRemoteToLocalMapping() {
//                return mapping;
//            }
//        };
//    }
//
//    @Test
//    public void loadPipeline01() throws Exception {
//        var input = TestUtils.statementsFromResource(
//                "pipeline/importer/01-input.trig");
//
//        TemplateStore store = new TemplateMemoryStore("http://localhost");
//        TemplatesData data = new TemplatesData(store);
//        data.templates.put(
//                "http://localhost/resources/components/e-sparqlEndpoint",
//                Template.Reference(null));
//
//        ImportTemplateOptions options = new ImportTemplateOptions();
//        var actual = createCommand(data, new HashMap<>()).apply(
//                options, Statements.wrap(input),
//                valueFactory.createIRI("http://localhost/pipeline"));
//
//        Set<Resource> actualGraph = new HashSet<>();
//        actual.unUsedStatements.forEach(st -> actualGraph.add(st.getContext()));
//        Assertions.assertIterableEquals(Arrays.asList(
//                valueFactory.createIRI("http://localhost/config/0"),
//                valueFactory.createIRI("http://localhost/pipeline")),
//                actualGraph
//        );
//
//        var templateMapping = actual.remoteToLocal;
//
//        Resource templateOne = valueFactory.createIRI(
//                "http://localhost/resources/components/1");
//        Assertions.assertTrue(templateMapping.containsKey(templateOne));
//
//    }
//
//    @Test
//    public void loadPipeline02() throws Exception {
//        var valueFactory = SimpleValueFactory.getInstance();
//
//        var templates = TestUtils.statementsFromResource(
//                "pipeline/importer/02-templates.trig");
//
//        TemplateStore store = new TemplateMemoryStore("http://localhost");
//        store.storeReferenceDefinition("http://localhost/first",
//                Statements.wrap(templates).selectByGraph(
//                        "http://localhost/first"));
//
//        store.storeReferenceDefinition("http://localhost/second",
//                Statements.wrap(templates).selectByGraph(
//                        "http://localhost/second"));
//
//        TemplatesData data = new TemplatesData(store);
//        data.templates.put(
//                "http://etl.linkedpipes.com/components/e-textHolder/0.0.0",
//                Template.Reference(null));
//
//        Map<String, String> mapping = new HashMap<>();
//        mapping.put(
//                "https://demo.etl.linkedpipes.com/918b11c20b3a",
//                "http://localhost/first");
//        mapping.put(
//                "https://demo.etl.linkedpipes.com/fbb905222f71",
//                "http://localhost/second");
//
//        ImportTemplateOptions options = new ImportTemplateOptions();
//        options.updateExistingConfiguration = true;
//
//        var input = TestUtils.statementsFromResource(
//                "pipeline/importer/02-input.trig");
//
//        var actual = createCommand(data, mapping).apply(
//                options, Statements.wrap(input),
//                valueFactory.createIRI("http://localhost:8080/pipeline"));
//
//        Set<Resource> actualGraph = new HashSet<>();
//        actual.unUsedStatements.forEach(st -> actualGraph.add(st.getContext()));
//        Assertions.assertIterableEquals(Arrays.asList(
//                valueFactory.createIRI("http://localhost:8080/pipeline"),
//                valueFactory.createIRI("http://localhost:8080/pipeline/4")),
//                actualGraph
//        );
//
//        var templateMapping = actual.remoteToLocal;
//
//        Resource templateOne = valueFactory.createIRI(
//                "http://localhost:8080/resources/components/" +
//                        "1622805775578-" +
//                        "ff2aa41d-16d7-413f-b44b-99b8902a76ad");
//        Assertions.assertTrue(templateMapping.containsKey(templateOne));
//
//        Resource templateTwo = valueFactory.createIRI(
//                "http://localhost:8080/resources/components/" +
//                        "1622805775596-" +
//                        "87fe062b-3710-434f-8fd2-598cac33b97c");
//        Assertions.assertTrue(templateMapping.containsKey(templateTwo));
//
//        Resource templateThree = valueFactory.createIRI(
//                "http://localhost:8080/resources/components/" +
//                        "1622805775592");
//        Assertions.assertTrue(templateMapping.containsKey(templateThree));
//
//        // http://localhost:8080/resources/components/1622805775578-ff2aa41d-16d7-413f-b44b-99b8902a76ad
//        // -> http://localhost/1623093587266-939c7614-a1de-4f9a-8fca-972632b5eec3
//
//        Assertions.assertEquals(
//                "http://localhost/first",
//                templateMapping.get(templateOne).stringValue()
//        );
//
//        Assertions.assertEquals(
//                "http://localhost/second",
//                templateMapping.get(templateTwo).stringValue()
//        );
//
//    }