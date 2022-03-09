package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineList;

import java.util.Collection;
import java.util.Optional;

public interface PipelineApi {

    /**
     * Return new pipeline IRI. IRI suffix can be optionally specified.
     * Caller may not decide to create a pipeline of reserved IRI.
     */
    String reservePipelineIri(String suffix) throws StorageException;

    Collection<String> listPipelines() throws StorageException;

    Optional<Pipeline> loadPipeline(String iri) throws StorageException;

    /**
     * Store given pipeline. If pipeline resource is null, only the pipeline
     * resource is set. Return pipeline as it was stored.
     */
    Pipeline storePipeline(Pipeline pipeline) throws StorageException;

    void deletePipeline(String iri) throws StorageException;

    /**
     * Create and return list of all stored pipelines.
     */
    PipelineList createPipelineList() throws StorageException;

}
