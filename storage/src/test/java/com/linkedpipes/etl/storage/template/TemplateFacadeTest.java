package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.store.InMemoryTestStore;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateService;
import com.linkedpipes.etl.storage.template.plugin.adapter.PluginTemplateAdapter;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateService;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateFacadeTest {

    @Test
    public void createNewTemplateWithNoParent() {
        InMemoryTestStore store = new InMemoryTestStore("http://local/");

        TemplateFacade templateFacade = new TemplateFacade(
                new PluginTemplateService(store),
                new ReferenceTemplateService(store));

        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf("template/reference/new-template.trig"));
        var template =
                ReferenceTemplateAdapter.asReferenceTemplates(input).get(0);

        try {
            templateFacade.storeReferenceTemplate(template);
            Assertions.fail("This should have raised an exception.");
        } catch (StorageException ex) {
            // Ignore.
        }

    }

    @Test
    public void createNewTemplate() throws StorageException {
        InMemoryTestStore store = new InMemoryTestStore("http://local/");

        var textHolder = TestUtils.statements(
                "template/plugin/text-holder.trig").selector();

        store.storePluginTemplate(
                PluginTemplateAdapter.asPluginTemplates(textHolder).get(0));

        TemplateFacade templateFacade = new TemplateFacade(
                new PluginTemplateService(store),
                new ReferenceTemplateService(store));

        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf("template/reference/new-template.trig"));
        var template =
                ReferenceTemplateAdapter.asReferenceTemplates(input).get(0);

        Resource resource = templateFacade.storeReferenceTemplate(template)
                .resource();

        Assertions.assertTrue(resource.isIRI());
    }

}
