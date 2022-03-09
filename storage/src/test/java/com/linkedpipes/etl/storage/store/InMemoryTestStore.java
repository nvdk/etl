package com.linkedpipes.etl.storage.store;

import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.store.Store;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryTestStore implements Store {

    public final Map<String, Pipeline> pipelines = new HashMap<>();

    public final Map<String, PluginTemplate> plugins = new HashMap<>();

    public final Map<String, ReferenceTemplate> references = new HashMap<>();

    private final String prefix;

    private Integer counter = 0;

    public InMemoryTestStore(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String basePipelineUrl() {
        return prefix + "pipeline/";
    }

    @Override
    public Collection<String> listPipelines() {
        return pipelines.keySet();
    }

    @Override
    public String reservePipelineIri(String suffix) {
        return basePipelineUrl() + (suffix == null ? ++counter : suffix);
    }

    @Override
    public Optional<Pipeline> loadPipeline(String iri) {
        return Optional.ofNullable(pipelines.get(iri));
    }

    @Override
    public void storePipeline(Pipeline pipeline) {
        pipelines.put(pipeline.resource().stringValue(), pipeline);
    }

    @Override
    public void deletePipeline(String iri) {
        pipelines.get(iri);
    }

    @Override
    public String getName() {
        return "memory";
    }

    @Override
    public Collection<String> listPluginTemplates() {
        return plugins.keySet();
    }

    @Override
    public Optional<PluginTemplate> loadPluginTemplate(String iri) {
        return Optional.empty();
    }

    @Override
    public void storePluginTemplate(PluginTemplate template) {
        plugins.put(template.resource().stringValue(), template);
    }

    @Override
    public Collection<String> listReferencesTemplate() {
        return references.keySet();
    }

    @Override
    public String reserveReferenceIri() {
        return prefix + "reference/" + ++counter;
    }

    @Override
    public Optional<ReferenceTemplate> loadReferenceTemplate(String iri) {
        return Optional.ofNullable(references.get(iri));
    }

    @Override
    public void storeReferenceTemplate(ReferenceTemplate template) {
        references.put(template.resource().stringValue(), template);
    }

    @Override
    public void deleteReferenceTemplate(String iri) {
        references.remove(iri);
    }

}
