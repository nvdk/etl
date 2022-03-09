package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.store.InMemoryTestStore;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ReferenceTemplateServiceTest {

    @Test
    public void updateConfigurationOnStore() throws StorageException {
        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf(
                "migration/v1/templates/1621865731462/configuration.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v1/templates/1621865731462/definition.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v1/templates/1621865731462/interface.trig"));

        var templates =
                ReferenceTemplateAdapter.asReferenceTemplates(input);

        Assertions.assertEquals(1, templates.size());
        var template = templates.get(0);

        // Set resource to null, thus component will be imported as new.
        template = new ReferenceTemplate(null,
                template.template(), template.prefLabel(),
                template.description(), template.note(),
                template.color(), template.tags(), template.knownAs(),
                template.pluginTemplate(), template.version(),
                template.configuration(), template.configurationGraph());

        InMemoryTestStore store = new InMemoryTestStore("http://local/");
        ReferenceTemplateService service = new ReferenceTemplateService(store);
        String result = service.storeReferenceTemplate(template)
                .resource().stringValue();

        Assertions.assertEquals("http://local/reference/1", result);
        var stored = store.references.get("http://local/reference/1");
        var configuration = stored.configuration();
        Assertions.assertEquals(6, configuration.size());
        for (Statement statement : configuration) {
            Assertions.assertEquals(
                    "http://local/reference/1/configuration/000",
                    statement.getSubject().stringValue());
            // Configuration is stored without a configuration graph.
            Assertions.assertEquals(null, statement.getContext());
        }
    }


}
