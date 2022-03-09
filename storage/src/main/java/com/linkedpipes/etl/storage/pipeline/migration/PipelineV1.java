package com.linkedpipes.etl.storage.pipeline.migration;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.reference.migration.PluginTemplateSource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PipelineV1 {

    private static class Mapping {

        public final String source;

        public final String target;

        public Mapping(String source, String target) {
            this.source = source;
            this.target = target;
        }
    }

    /**
     * For template with given root plugin, stores how should the
     * prefix in configuration should be changed.
     */
    private static final Map<String, Mapping> MAPPINGS;

    static {
        MAPPINGS = new HashMap<>();

        String prefix = "http://etl.linkedpipes.com/resources/components/";
        String suffix = "/0.0.0";

        MAPPINGS.put(prefix + "l-sparqlEndpointChunked" + suffix,
                new Mapping("l-sparqlEndpoint", "l-sparqlEndpointChunked"));
        MAPPINGS.put(prefix + "t-filesToRdfChunked" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfChunked"));
        MAPPINGS.put(prefix + "t-sparqlConstructChunked" + suffix,
                new Mapping("t-sparqlConstruct", "t-sparqlConstructChunked"));
        MAPPINGS.put(prefix + "t-filesToRdfGraph" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfGraph"));
        MAPPINGS.put(prefix + "t-filesToRdfChunked" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfChunked"));
        MAPPINGS.put(prefix + "t-mustacheChunked" + suffix,
                new Mapping("t-mustache", "t-mustacheChunked"));
        MAPPINGS.put(prefix + "t-sparqlUpdateChunked" + suffix,
                new Mapping("t-sparqlUpdate", "t-sparqlUpdateChunked"));
        MAPPINGS.put(prefix + "t-tabularChunked" + suffix,
                new Mapping("t-tabular", "t-tabularChunked"));
    }

    private final PluginTemplateSource templatesApi;

    public PipelineV1(PluginTemplateSource templatesApi) {
        this.templatesApi = templatesApi;
    }

    /**
     * Some components shared configuration types. This cause issues
     * when additional properties were added to one of the configurations.
     *
     * <p>The type is used to match the description. This cause that wrong
     * description may be used to merge the configuration. This result
     * in removal of some properties.
     *
     * <p>This bug depends on the order of components.
     *
     * <p>As a solution the configuration of the components is changed to use
     * different vocabularies.
     *
     * <p>Another thing is that a reference to a configuration description is
     * added to definition of reference templates.
     */
    public Pipeline migrateToV2(Pipeline pipeline)
            throws StorageException {
        List<PipelineComponent> components = new ArrayList<>();
        for (PipelineComponent component : pipeline.components()) {
            components.add(migrateComponent(component));
        }
        return new Pipeline(
                pipeline.resource(),
                pipeline.label(),
                pipeline.version(),
                pipeline.note(),
                pipeline.tags(),
                pipeline.executionProfile(),
                components,
                pipeline.connections()
        );
    }

    private PipelineComponent migrateComponent(PipelineComponent component)
            throws StorageException {
        String pluginTemplate = getPluginTemplate(
                component.template().stringValue());
        if (!shouldUpdateTemplateWithPlugin(pluginTemplate)) {
            return component;
        }
        return new PipelineComponent(
                component.resource(),
                component.label(),
                component.description(),
                component.note(),
                component.color(),
                component.xPosition(),
                component.yPosition(),
                component.template(),
                component.disabled(),
                component.configurationGraph(),
                updateConfiguration(pluginTemplate, component.configuration()));
    }

    private String getPluginTemplate(String iri) throws StorageException {
        String template = templatesApi.getPluginTemplate(iri);
        if (template == null) {
            throw new StorageException("Missing template: " + iri);
        }
        return template;
    }

    private boolean shouldUpdateTemplateWithPlugin(String iri) {
        return MAPPINGS.containsKey(iri);
    }

    private Statements updateConfiguration(
            String pluginTemplate, Statements statements) {
        Mapping mapping = MAPPINGS.get(pluginTemplate);
        if (mapping == null) {
            return statements;
        }
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        return Statements.wrap(statements.stream().map((statement -> {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                return valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        updateIRI((IRI) statement.getObject(), mapping),
                        statement.getContext()
                );
            }
            return valueFactory.createStatement(
                    statement.getSubject(),
                    updateIRI(statement.getPredicate(), mapping),
                    statement.getObject(),
                    statement.getContext());
        })).collect(Collectors.toList()));
    }

    private static IRI updateIRI(IRI source, Mapping mapping) {
        String mapped = source.stringValue().replace(
                mapping.source, mapping.target);
        return SimpleValueFactory.getInstance().createIRI(mapped);
    }

}
