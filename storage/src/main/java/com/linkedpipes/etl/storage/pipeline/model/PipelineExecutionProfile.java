package com.linkedpipes.etl.storage.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public record PipelineExecutionProfile(
        Resource resource,
        Resource rdfRepositoryPolicy,
        Resource rdfRepositoryType
) {

    private static final IRI DEFAULT_POLICY;

    private static final IRI DEFAULT_TYPE;

    static {
        ValueFactory factory = SimpleValueFactory.getInstance();
        DEFAULT_POLICY = factory.createIRI(LP_PIPELINE.SINGLE_REPOSITORY);
        DEFAULT_TYPE = factory.createIRI(LP_PIPELINE.NATIVE_STORE);
    }

    public PipelineExecutionProfile(Resource resource) {
        this(
                resource,
                DEFAULT_POLICY,
                DEFAULT_TYPE
        );
    }

}
