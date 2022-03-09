package com.linkedpipes.etl.storage.exporter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.importer.ImportContent;
import com.linkedpipes.etl.storage.importer.ImportOptions;
import com.linkedpipes.etl.storage.pipeline.PipelineService;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.store.InMemoryTestStore;
import com.linkedpipes.etl.storage.store.ReadOnlyLegacyStore;
import com.linkedpipes.etl.storage.store.Store;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateService;
import com.linkedpipes.etl.storage.template.plugin.adapter.PluginTemplateAdapter;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateService;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.plugin.loader.Plugin;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;

public class ExportContentTest {

    static class LegacyStore extends ReadOnlyLegacyStore {

        protected Map<String, PluginTemplate> plugins = new HashMap<>();

        public LegacyStore(File storeDirectory, String baseUrl) {
            super(storeDirectory, baseUrl, 4);
        }

        @Override
        public Collection<String> listPluginTemplates() {
            return plugins.keySet();
        }

        @Override
        public Optional<PluginTemplate> loadPluginTemplate(String iri) {
            return Optional.ofNullable(plugins.get(iri));
        }

        @Override
        public void storePluginTemplate(PluginTemplate template) {
            plugins.put(template.resource().stringValue(), template);
        }
    }

    @Test
    public void exportVersion4() throws StorageException {
        Store sourceStore = new LegacyStore(
                TestUtils.file("migration/v4"), "http://localhost:8080");
        sourceStore.storePluginTemplate(loadTextHolder());

        PipelineService pipelinesApi = new PipelineService(sourceStore);
        TemplateFacade templatesApi = new TemplateFacade(
                new PluginTemplateService(sourceStore),
                new ReferenceTemplateService(sourceStore));

        ExportContent exportWorker = new ExportContent(
                templatesApi, new ExportOptions(true, true));

        List<Pipeline> pipelines = new ArrayList<>();
        for (String iri : pipelinesApi.listPipelines()) {
            pipelines.add(pipelinesApi.loadPipeline(iri).get());
        }
        ExportContentData exportData = exportWorker.exportPipelines(pipelines);
        Statements actual = Statements.arrayList();
        exportData.pipelines().forEach(
                pipeline -> actual.addAll(
                        PipelineAdapter.asRdf(pipeline)));
        exportData.referenceTemplates().forEach(
                template -> actual.addAll(
                        ReferenceTemplateAdapter.asRdf(template)));

        Statements expected = TestUtils.statements(
                "exporter/migration-v4-export.trig");
        TestUtils.assertIsomorphic(expected, actual);

        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Store targetStore = new InMemoryTestStore("http://localhost:8080");
        targetStore.storePluginTemplate(new PluginTemplate(
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/resources/components/" +
                                "e-textHolder/0.0.0"),
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null));

        ImportContent importWorker = new ImportContent(
                new PipelineService(targetStore),
                new TemplateFacade(
                        new PluginTemplateService(targetStore),
                        new ReferenceTemplateService(targetStore)));
        importWorker.importStatements(actual, new ImportOptions(
                Collections.emptyMap(), true, true));

        // As import will update versions we can not check the actual content.
        Assertions.assertEquals(
                sourceStore.listPipelines().size(),
                targetStore.listPipelines().size());
        Assertions.assertEquals(
                sourceStore.listReferencesTemplate().size(),
                targetStore.listReferencesTemplate().size());
    }

    private PluginTemplate loadTextHolder() throws StorageException {
        var statements = TestUtils.statements(
                "template/plugin/text-holder.trig").selector();

        var fileEntries = new HashMap<String, JarEntry>();
        fileEntries.put("config/dialog.js", null);
        fileEntries.put("config/dialog.html", null);
        fileEntries.put("template/dialog.js", null);
        fileEntries.put("template/dialog.html", null);
        fileEntries.put("instance/dialog.js", null);
        fileEntries.put("instance/dialog.html", null);

        var input = new Plugin(
                null,
                null,
                null,
                null,
                statements.selectByGraph("http://definition")
                        .asList(),
                statements.selectByGraph("http://configuration")
                        .asList(),
                statements.selectByGraph("http://configuration-description")
                        .asList(),
                fileEntries
        );

        return PluginTemplateAdapter.asPluginTemplate(input);
    }

}
