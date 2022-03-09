package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.storage.store.InMemoryTestStore;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class PipelineServiceTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void createPipelineFromBlankNode() throws StorageException {
        InMemoryTestStore store = new InMemoryTestStore(
                "http://localhost/resources/");
        PipelineService service = new PipelineService(store);

        Pipeline pipeline = new Pipeline(
                valueFactory.createBNode(),
                null, valueFactory.createLiteral(5),
                null, Collections.emptyList(),
                new PipelineExecutionProfile(valueFactory.createBNode()),
                Collections.emptyList(), Collections.emptyList());

        Pipeline actual = service.storePipeline(pipeline);

        Assertions.assertTrue(actual.resource().isIRI());
        Assertions.assertNotNull(actual.executionProfile());
        Assertions.assertNotNull(actual.executionProfile().resource());
        Assertions.assertTrue(actual.executionProfile().resource().isIRI());
    }

}
