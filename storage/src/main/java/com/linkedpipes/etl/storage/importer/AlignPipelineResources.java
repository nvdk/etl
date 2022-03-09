package com.linkedpipes.etl.storage.importer;

import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.pipeline.model.PipelineConnection;
import com.linkedpipes.etl.storage.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionFlow;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.storage.rdf.UpdateResources;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Change pipeline resource and propagate the changes to all parts
 * of the pipeline.
 */
public class AlignPipelineResources {

    private final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    private Map<Resource, Resource> componentMapping = new HashMap<>();

    protected AlignPipelineResources() {
    }

    public static Pipeline apply(Pipeline pipeline, Resource resource) {
        Pipeline pipelineWithResource = new Pipeline(
                resource, pipeline.label(), pipeline.version(),
                pipeline.note(), pipeline.tags(), pipeline.executionProfile(),
                pipeline.components(), pipeline.connections());
        return (new AlignPipelineResources())
                .alignPipeline(pipelineWithResource);
    }

    public static Pipeline apply(Pipeline pipeline) {
        if (pipeline.resource() == null) {
            return pipeline;
        }
        return (new AlignPipelineResources()).alignPipeline(pipeline);
    }

    private Pipeline alignPipeline(Pipeline pipeline) {
        String baseUrl = pipeline.resource().stringValue();
        return new Pipeline(
                pipeline.resource(), pipeline.label(), pipeline.version(),
                pipeline.note(), pipeline.tags(),
                alignProfile(baseUrl, pipeline.executionProfile()),
                alignComponents(baseUrl, pipeline.components()),
                alignConnections(baseUrl, pipeline.connections()));

    }

    private PipelineExecutionProfile alignProfile(
            String baseUrl, PipelineExecutionProfile profile) {
        if (profile == null) {
            return null;
        }
        return new PipelineExecutionProfile(
                valueFactory.createIRI(baseUrl + "/profile"),
                profile.rdfRepositoryPolicy(),
                profile.rdfRepositoryType());
    }

    private List<PipelineComponent> alignComponents(
            String baseUrl, List<PipelineComponent> components) {
        List<PipelineComponent> result = new ArrayList<>(components.size());
        for (int index = 0; index < components.size(); ++index) {
            result.add(alignComponent(
                    baseUrl + "/components/", index, components.get(index)));
        }
        return result;
    }

    private String asIndex(Integer index) {
        return "0000-" + String.format("%04d", index);
    }

    private PipelineComponent alignComponent(
            String baseUrl, Integer index, PipelineComponent component) {
        String config = baseUrl + asIndex(index) + "/configuration";
        Resource resource = valueFactory.createIRI(baseUrl + asIndex(index));
        componentMapping.put(component.resource(), resource);
        return new PipelineComponent(
                resource,
                component.label(), component.description(), component.note(),
                component.color(), component.xPosition(), component.yPosition(),
                component.template(), component.disabled(),
                valueFactory.createIRI(config),
                UpdateResources.apply(config + "/", component.configuration())
        );
    }

    private List<PipelineConnection> alignConnections(
            String baseUrl, List<PipelineConnection> connections) {
        List<PipelineConnection> result = new ArrayList<>(connections.size());
        for (int index = 0; index < connections.size(); ++index) {
            result.add(alignConnection(
                    baseUrl + "/connections/", index, connections.get(index)));
        }
        return result;
    }

    private PipelineConnection alignConnection(
            String baseUrl, Integer index, PipelineConnection connection) {
        if (connection instanceof PipelineDataFlow flow) {
            return new PipelineDataFlow(
                    valueFactory.createIRI(baseUrl + asIndex(index)),
                    componentMapping.get(flow.source()),
                    componentMapping.get(flow.target()),
                    flow.sourceBinding(),
                    flow.targetBinding());
        } else {
            return new PipelineExecutionFlow(
                    valueFactory.createIRI(baseUrl + asIndex(index)),
                    componentMapping.get(connection.source()),
                    componentMapping.get(connection.target()));
        }
    }

}
