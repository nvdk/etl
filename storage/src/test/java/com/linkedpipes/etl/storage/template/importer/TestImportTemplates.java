package com.linkedpipes.etl.storage.template.importer;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.store.StatementsTemplateStore;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TestImportTemplates {

    private static final String PLUGIN_PREFIX =
            "http://etl.linkedpipes.com/resources/components/";

    private static final List<String> PLUGIN_TEMPLATES = Arrays.asList(
            PLUGIN_PREFIX + "t-sparqlConstructChunked/0.0.0",
            PLUGIN_PREFIX + "e-textHolder/0.0.0"
    );

    private static class StubTemplateEventListener
            implements TemplateEventListener {

        final Map<String, String> knownAs = new HashMap<>();

        @Override
        public void onReferenceTemplateCreated(ReferenceContainer container) {
            if (container.definition.knownAs != null) {
                knownAs.put(
                        container.definition.knownAs.stringValue(),
                        container.resource.stringValue());
            }
        }
    }

    @Test
    public void importPipelineV1() throws Exception {
        var statements = TestUtils.rdfFromResource(
                "template/importer/pipeline-v1.trig");
        StatementsTemplateStore store = new StatementsTemplateStore();
        ImportTemplates worker = new ImportTemplates(
                new StubTemplateEventListener(), store,
                "http://localhost:8080",
                PLUGIN_TEMPLATES::contains,
                (iri) -> null);

        var result = worker.importFromStatement(statements, 1);
        Assertions.assertTrue(result.ignoredTemplates.isEmpty());
        Assertions.assertTrue(result.updatedTemplates.isEmpty());
        Assertions.assertEquals(1, result.localizedTemplates.size());

        Resource inputTemplate = SimpleValueFactory.getInstance().createIRI(
                "http://example.com/components/1621865731462");

        Resource importedAs = result.localizedTemplates.get(inputTemplate);
        Assertions.assertEquals(
                "http://localhost:8080/resources/components/1",
                importedAs.stringValue());

        var expectedTemplates = TestUtils.rdfFromResource(
                "template/importer/pipeline-v1.templates.trig");
        TestUtils.assertIsomorphic(expectedTemplates, store.getStatements());
    }

    @Test
    public void mapPipelineV1() {
        var statements = TestUtils.rdfFromResource(
                "template/importer/pipeline-v1.trig");
        StatementsTemplateStore store = new StatementsTemplateStore();
        ImportTemplates worker = new ImportTemplates(
                new StubTemplateEventListener(), store,
                "http://localhost:8080",
                PLUGIN_TEMPLATES::contains,
                (iri) -> null);

        var result = worker.mapFromStatement(statements, 1);
        Assertions.assertEquals(1, result.ignoredTemplates.size());
        Assertions.assertTrue(result.updatedTemplates.isEmpty());
        Assertions.assertTrue(result.localizedTemplates.isEmpty());
    }

    /**
     * Test that a mapping can be loaded from the pipeline.
     */
    @Test
    public void importPipelineV1WithMapping() throws Exception {
        var statements = TestUtils.rdfFromResource(
                "template/importer/pipeline-v1-mapping.trig");
        StatementsTemplateStore store = new StatementsTemplateStore();
        ImportTemplates worker = new ImportTemplates(
                new StubTemplateEventListener(), store,
                "http://localhost:8080",
                PLUGIN_TEMPLATES::contains,
                (iri) -> null);

        var result = worker.importFromStatement(statements, 1);
        Assertions.assertTrue(result.ignoredTemplates.isEmpty());
        Assertions.assertTrue(result.updatedTemplates.isEmpty());
        Assertions.assertEquals(1, result.localizedTemplates.size());

        var expectedTemplates = TestUtils.rdfFromResource(
                "template/importer/pipeline-v1-mapping.templates.trig");
        TestUtils.assertIsomorphic(expectedTemplates, store.getStatements());
    }

    @Test
    public void importPipelineV5() throws Exception {
        var statements = TestUtils.rdfFromResource(
                "template/importer/pipeline-v5.trig");
        StatementsTemplateStore store = new StatementsTemplateStore();
        ImportTemplates worker = new ImportTemplates(
                new StubTemplateEventListener(), store,
                "http://localhost:8080",
                PLUGIN_TEMPLATES::contains,
                (iri) -> null);

        var result = worker.importFromStatement(statements, 0);
        Assertions.assertTrue(result.ignoredTemplates.isEmpty());
        Assertions.assertTrue(result.updatedTemplates.isEmpty());
        Assertions.assertEquals(2, result.localizedTemplates.size());

        var factory = SimpleValueFactory.getInstance();
        Assertions.assertEquals(
                "http://localhost:8080/resources/components/1",
                result.localizedTemplates.get(factory.createIRI(
                        "http://example.com/components/1621937417354"))
                        .stringValue());
        Assertions.assertEquals(
                "http://localhost:8080/resources/components/2",
                result.localizedTemplates.get(factory.createIRI(
                        "http://example.com/components/1621937467650"))
                        .stringValue());

        var expectedTemplates = TestUtils.rdfFromResource(
                "template/importer/pipeline-v5.templates.trig");
        TestUtils.assertIsomorphic(expectedTemplates, store.getStatements());
    }

    /**
     * This test should not create redundant templates as they should be
     * imported only in the first pipeline import. The second import
     * only change one of the templates configuration.
     */
    @Test
    public void importPipelineV5AndV5Second() throws Exception {
        var statements = TestUtils.rdfFromResource(
                "template/importer/pipeline-v5.trig");
        StatementsTemplateStore store = new StatementsTemplateStore();
        // For this test we need the knownAs template source we use listener
        // to get this functionality.
        var listener = new StubTemplateEventListener();
        ImportTemplates worker = new ImportTemplates(
                listener, store,
                "http://localhost:8080",
                PLUGIN_TEMPLATES::contains,
                listener.knownAs::get);
        worker.importFromStatement(statements, 0);

        // Now import template that is knowAs the template from the
        // previous import.
        var nextStatements = TestUtils.rdfFromResource(
                "template/importer/templates-v5-second.trig");
        var result = worker.importFromStatement(nextStatements, 0);
        Assertions.assertTrue(result.ignoredTemplates.isEmpty());
        Assertions.assertEquals(1, result.updatedTemplates.size());
        Assertions.assertTrue(result.localizedTemplates.isEmpty());

        var expectedTemplates = TestUtils.rdfFromResource(
                "template/importer/templates-v5-second.templates.trig");
        TestUtils.assertIsomorphic(expectedTemplates, store.getStatements());
    }

}
