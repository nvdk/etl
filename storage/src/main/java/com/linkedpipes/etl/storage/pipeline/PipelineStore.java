package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;

import java.util.Collection;
import java.util.Optional;

public interface PipelineStore {

    String basePipelineUrl();

    Collection<String> listPipelines() throws StorageException;

    /**
     * Return new pipeline IRI, if suffix is given the suffix should be used.
     */
    String reservePipelineIri(String suffix) throws StorageException;

    Optional<Pipeline> loadPipeline(String iri) throws StorageException;

    void storePipeline(Pipeline pipeline) throws StorageException;

    void deletePipeline(String iri) throws StorageException;

}
