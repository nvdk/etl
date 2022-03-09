package com.linkedpipes.etl.storage.pipeline.assistant;

import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.pipeline.model.PipelineDataFlow;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AssistantServiceTest {

    @Test
    public void operationTest() {
        AssistantService service = new AssistantService();
        Assertions.assertTrue(service.getData().tags().isEmpty());
        Assertions.assertTrue(service.getData().followup().isEmpty());

        ValueFactory valueFactory = SimpleValueFactory.getInstance();

        Resource sourceNode = valueFactory.createBNode();
        PipelineComponent source = new PipelineComponent(
                sourceNode, null, null, null, null, null, null,
                valueFactory.createIRI("http://source"),
                null, null, null);

        Resource targetNode = valueFactory.createBNode();
        PipelineComponent target = new PipelineComponent(
                targetNode, null, null, null, null, null, null,
                valueFactory.createIRI("http://target"),
                null, null, null);

        Value sourceBinding = valueFactory.createLiteral("source");
        Value targetBinding = valueFactory.createLiteral("target");
        PipelineDataFlow dataFlow = new PipelineDataFlow(
                null, sourceNode, targetNode, sourceBinding, targetBinding);

        Literal testTag = valueFactory.createLiteral("test");
        Pipeline first = new Pipeline(
                valueFactory.createBNode(),
                null, null, null,
                List.of(testTag),
                null, Arrays.asList(source, target), List.of(dataFlow));

        service.updatePipeline(first);
        Assertions.assertEquals(1, service.getData().tags().size());
        Assertions.assertTrue(service.getData().tags().containsKey(testTag));
        Assertions.assertEquals(1, service.getData().followup().size());

        Literal otherTag = valueFactory.createLiteral("other");
        Pipeline second = new Pipeline(
                valueFactory.createBNode(),
                null, null, null,
                Arrays.asList(testTag, otherTag),
                null, Arrays.asList(source, target), List.of(dataFlow));

        service.updatePipeline(second);
        Assertions.assertEquals(2, service.getData().tags().size());
        Assertions.assertTrue(service.getData().tags().containsKey(testTag));
        Assertions.assertTrue(service.getData().tags().containsKey(otherTag));
        Assertions.assertEquals(1, service.getData().followup().size());
        var entry = service.getData().followup().entrySet().iterator().next();
        Assertions.assertEquals(2, entry.getValue());

        service.deletePipeline(second.resource().stringValue());
        Assertions.assertEquals(1, service.getData().tags().size());
        Assertions.assertEquals(1, service.getData().followup().size());

        service.deletePipeline(first.resource().stringValue());
        Assertions.assertTrue(service.getData().tags().isEmpty());
        Assertions.assertTrue(service.getData().followup().isEmpty());

    }

}
