package com.linkedpipes.etl.storage.template.reference.adapter;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.reference.migration.MigrateReferenceTemplate;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ReferenceTemplateAdapterTest {

    @Test
    public void loadMigrationV1() throws Exception {
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

        MigrateReferenceTemplate worker = createMigrationWorker();
        template = worker.migrate(template);

        var actual = Statements.arrayList();
        actual.addAll(ReferenceTemplateAdapter.definitionAsRdf(
                template));
        actual.addAll(ReferenceTemplateAdapter.configurationAsRdf(
                template));

        var expected = TestUtils.rdf(
                "migration/v1/expected-templates.trig");

        TestUtils.assertIsomorphic(expected, actual);
    }

    private MigrateReferenceTemplate createMigrationWorker() {
        Map<String, String> roots = new HashMap<>();
        roots.put(
                "http://localhost:8080/resources/components/1621865731462",
                "http://etl.linkedpipes.com/resources/components/t-sparqlConstructChunked/0.0.0");
        roots.put(
                "http://localhost:8080/resources/components/1621875215693-45315619-badd-4c3d-8f43-0f3ab961a375",
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0");
        roots.put(
                "http://localhost:8080/resources/components/1621880885677-0ead2138-65f0-4776-91eb-4ff6a7234316",
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0");
        roots.put(
                "http://localhost:8080/resources/components/1621937417354-2fd32ec7-ed52-4013-bf2b-0ba950f044c3",
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0");
        roots.put(
                "http://localhost:8080/resources/components/1621937467650-899a4740-78e7-4ef5-a1ee-3fbc4c2a0b1c",
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0");
        return new MigrateReferenceTemplate(roots::get);
    }

    @Test
    public void loadMigrationV2() throws Exception {
        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf(
                "migration/v2/templates/1621875215693-45315619-badd-4c3d-8f43-0f3ab961a375/configuration.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v2/templates/1621875215693-45315619-badd-4c3d-8f43-0f3ab961a375/definition.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v2/templates/1621875215693-45315619-badd-4c3d-8f43-0f3ab961a375/interface.trig"));

        var templates =
                ReferenceTemplateAdapter.asReferenceTemplates(input);

        Assertions.assertEquals(1, templates.size());
        var template = templates.get(0);

        MigrateReferenceTemplate worker = createMigrationWorker();
        template = worker.migrate(template);

        var actual = Statements.arrayList();
        actual.addAll(ReferenceTemplateAdapter.definitionAsRdf(
                template));
        actual.addAll(ReferenceTemplateAdapter.configurationAsRdf(
                template));

        var expected = TestUtils.rdf(
                "migration/v2/expected-templates.trig");

        TestUtils.assertIsomorphic(expected, actual);
    }

    @Test
    public void loadMigrationV3() throws Exception {
        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf(
                "migration/v3/templates/1621880885677-0ead2138-65f0-4776-91eb-4ff6a7234316/configuration.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v3/templates/1621880885677-0ead2138-65f0-4776-91eb-4ff6a7234316/definition.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v3/templates/1621880885677-0ead2138-65f0-4776-91eb-4ff6a7234316/interface.trig"));

        var templates =
                ReferenceTemplateAdapter.asReferenceTemplates(input);

        Assertions.assertEquals(1, templates.size());
        var template = templates.get(0);

        MigrateReferenceTemplate worker = createMigrationWorker();
        template = worker.migrate(template);

        var actual = Statements.arrayList();
        actual.addAll(ReferenceTemplateAdapter.definitionAsRdf(
                template));
        actual.addAll(ReferenceTemplateAdapter.configurationAsRdf(
                template));

        var expected = TestUtils.rdf(
                "migration/v3/expected-templates.trig");

        TestUtils.assertIsomorphic(expected, actual);
    }

    @Test
    public void loadMigrationV4() throws Exception {
        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf(
                "migration/v4/templates/1621937417354-2fd32ec7-ed52-4013-bf2b-0ba950f044c3/configuration.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v4/templates/1621937417354-2fd32ec7-ed52-4013-bf2b-0ba950f044c3/definition.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v4/templates/1621937417354-2fd32ec7-ed52-4013-bf2b-0ba950f044c3/interface.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v4/templates/1621937467650-899a4740-78e7-4ef5-a1ee-3fbc4c2a0b1c/configuration.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v4/templates/1621937467650-899a4740-78e7-4ef5-a1ee-3fbc4c2a0b1c/definition.trig"));
        input.addAll(TestUtils.rdf(
                "migration/v4/templates/1621937467650-899a4740-78e7-4ef5-a1ee-3fbc4c2a0b1c/interface.trig"));

        var templates =
                ReferenceTemplateAdapter.asReferenceTemplates(input);

        Assertions.assertEquals(2, templates.size());

        MigrateReferenceTemplate worker = createMigrationWorker();
        var first = worker.migrate(templates.get(0));
        var second = worker.migrate(templates.get(1));

        var actual = Statements.arrayList();
        actual.addAll(ReferenceTemplateAdapter.definitionAsRdf(
                first));
        actual.addAll(ReferenceTemplateAdapter.configurationAsRdf(
                first));
        actual.addAll(ReferenceTemplateAdapter.definitionAsRdf(
                second));
        actual.addAll(ReferenceTemplateAdapter.configurationAsRdf(
                second));

        var expected = TestUtils.rdf(
                "migration/v4/expected-templates.trig");

        TestUtils.assertIsomorphic(expected, actual);
    }

    @Test
    public void loadMigrationV5() {

    }

    @Test
    public void loadNewTemplate() {
        var input = Statements.arrayList();
        input.addAll(TestUtils.rdf("template/reference/new-template.trig"));

        var templates = ReferenceTemplateAdapter.asReferenceTemplates(input);

        Assertions.assertEquals(1, templates.size());
        var template = templates.get(0);

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0",
                template.template().stringValue());

        Assertions.assertEquals(
                5, template.version().intValue());

        Assertions.assertEquals(
                1, template.configuration().size());
    }

}
