package com.linkedpipes.etl.storage.template.reference.importer;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImportReferenceTemplateFromRdfTest {

    public static class TemplatesApiMock implements TemplateApi {

        public Map<String, PluginTemplate> plugins = new HashMap<>();

        public Map<String, ReferenceTemplate> references = new HashMap<>();

        public TemplatesApiMock() {
            String prefix = "http://etl.linkedpipes.com/resources/components/";
            plugins.put(prefix + "e-httpGetFile/0.0.0", null);
            plugins.put(prefix + "e-sparqlEndpoint/0.0.0", null);
            plugins.put(prefix + "t-sparqlConstruct/0.0.0", null);
        }

        @Override
        public boolean isPluginTemplate(String iri) {
            return plugins.containsKey(iri);
        }

        @Override
        public boolean isReferenceTemplate(String iri) {
            return references.containsKey(iri);
        }

        @Override
        public Optional<String> getParent(String iri) {
            ReferenceTemplate template = references.get(iri);
            if (template == null) {
                return Optional.empty();
            }
            return Optional.of(template.template().stringValue());
        }

        @Override
        public List<String> getAncestors(String iri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> getSuccessors(String iri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Statements> getEffectiveConfiguration(String iri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Statements> getNewConfiguration(String iri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> listPluginTemplates() {
            return plugins.keySet();
        }

        @Override
        public Optional<PluginTemplate> loadPluginTemplate(String iri) {
            return Optional.of(plugins.get(iri));
        }

        @Override
        public Collection<String> listReferenceTemplates() {
            return references.keySet();
        }

        @Override
        public Optional<ReferenceTemplate> loadReferenceTemplate(String iri) {
            return Optional.of(references.get(iri));
        }

        @Override
        public ReferenceTemplate storeReferenceTemplate
                (ReferenceTemplate template) {
            ReferenceTemplate result;
            if (template.resource() == null) {
                IRI resource = SimpleValueFactory.getInstance().createIRI(
                        "http://templates/" + references.size());
                result = new ReferenceTemplate(
                        resource, template.template(),
                        template.prefLabel(), template.description(),
                        template.note(), template.color(), template.tags(),
                        template.knownAs(), template.pluginTemplate(),
                        template.version(), template.configuration(),
                        template.configurationGraph());
            } else {
                result = new ReferenceTemplate(template);
            }
            references.put(result.resource().stringValue(), result);
            return result;
        }

        @Override
        public void deleteReferenceTemplate(String iri) {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    void importPipeline000() throws StorageException {
        var statements = TestUtils.statements("./importer/pipeline-000.trig");
        var templates = new TemplatesApiMock();
        var worker = new ImportReferenceTemplate(templates);
        var mapping = worker.importReferenceTemplates(statements);
        Assertions.assertEquals(5, mapping.size());
    }

    @Test
    void importPipeline000WithKnown() throws StorageException {
        var statements = TestUtils.statements("./importer/pipeline-000.trig");
        var templates = new TemplatesApiMock();
        var valueFactory = SimpleValueFactory.getInstance();

        var template = new ReferenceTemplate(
                valueFactory.createIRI("http://local/1"), null,
                null, null, null, null, null,
                valueFactory.createIRI("https://example.com/resources/components/14779"),
                null, null, null, null
        );
        templates.storeReferenceTemplate(template);

        template = new ReferenceTemplate(
                valueFactory.createIRI("http://local/2"), null,
                null, null, null, null, null,
                valueFactory.createIRI("https://etl.nakit.opendata.cz/resources/components/1537"),
                null, null, null, null
        );
        templates.storeReferenceTemplate(template);

        var worker = new ImportReferenceTemplate(templates);
        var mapping = worker.importReferenceTemplates(statements);
        Assertions.assertEquals(5, mapping.size());
        Assertions.assertEquals(
                valueFactory.createIRI("http://local/1"),
                mapping.get(valueFactory.createIRI(
                        "https://example.com/resources/components/14779")));
        Assertions.assertEquals(
                valueFactory.createIRI("http://local/2"),
                mapping.get(valueFactory.createIRI(
                        "https://example.com/resources/components/16361")));
    }

}
