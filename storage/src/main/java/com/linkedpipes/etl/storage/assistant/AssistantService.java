package com.linkedpipes.etl.storage.assistant;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.pipeline.model.PipelineConnection;
import com.linkedpipes.etl.storage.pipeline.model.PipelineDataFlow;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssistantService implements AssistantApi {

    private record PipelineData(
            /*
             * Generated assistant data.
             */
            AssistantData assistant,
            /*
             * All templates used in this pipeline.
             */
            Set<Resource> templates
    ) {

    }

    /**
     * Aggregated data for all pipelines.
     */
    private AssistantData data;

    private final Map<String, PipelineData> perPipelineData = new HashMap<>();

    private final Object lock = new Object();

    public AssistantService() {
        data = new AssistantData(
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    public void initialize(PipelineApi pipelineApi) throws StorageException {
        for (String iri : pipelineApi.listPipelines()) {
            Optional<Pipeline> pipelineOptional = pipelineApi.loadPipeline(iri);
            if (pipelineOptional.isEmpty()) {
                continue;
            }
            updatePipeline(pipelineOptional.get());
        }
    }

    public void updatePipeline(Pipeline pipeline) {
        Map<Literal, Integer> tags = new HashMap<>();
        for (Literal tag : pipeline.tags()) {
            tags.put(tag, 1);
        }

        Map<Resource, Resource> componentToTemplate = new HashMap<>();
        for (PipelineComponent component : pipeline.components()) {
            componentToTemplate.put(component.resource(), component.template());
        }

        Map<AssistantData.ComponentPair, Integer> counts = new HashMap<>();
        for (PipelineConnection connection : pipeline.connections()) {
            if (connection instanceof PipelineDataFlow flow) {
                AssistantData.ComponentPair pair = asComponentPair(
                        componentToTemplate, flow);
                counts.put(pair, counts.getOrDefault(pair, 0) + 1);
            }
        }

        Set<Resource> templates = new HashSet<>(componentToTemplate.values());
        perPipelineData.put(pipeline.resource().stringValue(),
                new PipelineData(new AssistantData(tags, counts), templates));

        synchronized (lock) {
            regenerateData();
        }
    }

    private AssistantData.ComponentPair asComponentPair(
            Map<Resource, Resource> componentToTemplate,
            PipelineDataFlow connection) {
        return new AssistantData.ComponentPair(
                componentToTemplate.get(connection.source()),
                connection.sourceBinding(),
                componentToTemplate.get(connection.target()),
                connection.targetBinding());
    }

    private void regenerateData() {
        Map<Literal, Integer> tags = new HashMap<>();
        Map<AssistantData.ComponentPair, Integer> counts = new HashMap<>();

        for (PipelineData pipelineData : perPipelineData.values()) {
            AssistantData assistantData = pipelineData.assistant;
            for (var entry : assistantData.tags().entrySet()) {
                Literal key = entry.getKey();
                tags.put(key, entry.getValue() + tags.getOrDefault(key, 0));
            }
            for (var entry : assistantData.followup().entrySet()) {
                AssistantData.ComponentPair key = entry.getKey();
                counts.put(key, entry.getValue() + counts.getOrDefault(key, 0));
            }
        }

        this.data = new AssistantData(tags, counts);
    }

    public void deletePipeline(String pipelineIri) {
        perPipelineData.remove(pipelineIri);
        synchronized (lock) {
            regenerateData();
        }
    }

    @Override
    public AssistantData getData() {
        return this.data;
    }

    public PipelineIriList pipelinesWithTemplate(String template) {
        Resource resource =
                SimpleValueFactory.getInstance().createIRI(template);
        PipelineIriList result = new PipelineIriList();
        for (var entry : perPipelineData.entrySet()) {
            if (entry.getValue().templates.contains(resource)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Wrap given API and make sure assistant service is properly called.
     */
    public PipelineApi wrapPipelineApi(PipelineApi pipelineApi) {
        return new AssistantPipelineApiWrap(pipelineApi, this);
    }


}
