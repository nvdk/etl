package com.linkedpipes.etl.storage.importer;

import com.linkedpipes.etl.storage.pipeline.PipelineService;
import com.linkedpipes.etl.storage.store.InMemoryTestStore;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateService;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateService;
import org.junit.jupiter.api.Test;

public class ImportContentTest {

    @Test
    public void import000() {
        InMemoryTestStore store = new InMemoryTestStore("http://localhost");
        PluginTemplateService templateService =
                new PluginTemplateService(store);
        ReferenceTemplateService referenceService =
                new ReferenceTemplateService(store);
        TemplateFacade templateFacade =
                new TemplateFacade(templateService, referenceService);
        PipelineService pipelineFacade =
                new PipelineService(store);
        ImportContent worker = new ImportContent(
                pipelineFacade, templateFacade);

    }

}
