package com.linkedpipes.etl.storage.assistant;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineList;

import java.util.Collection;
import java.util.Optional;

class AssistantPipelineApiWrap implements PipelineApi {

    private final PipelineApi pipelineApi;

    private final AssistantService assistantApi;

    public AssistantPipelineApiWrap(
            PipelineApi pipelineApi, AssistantService assistantApi) {
        this.pipelineApi = pipelineApi;
        this.assistantApi = assistantApi;
    }

    @Override
    public String reservePipelineIri(String suffix) throws StorageException {
        return pipelineApi.reservePipelineIri(suffix);
    }

    @Override
    public Collection<String> listPipelines() throws StorageException {
        return pipelineApi.listPipelines();
    }

    @Override
    public Optional<Pipeline> loadPipeline(String iri) throws StorageException {
        return pipelineApi.loadPipeline(iri);
    }

    @Override
    public Pipeline storePipeline(Pipeline pipeline) throws StorageException {
        Pipeline result = pipelineApi.storePipeline(pipeline);
        assistantApi.updatePipeline(pipeline);
        return result;
    }

    @Override
    public void deletePipeline(String iri) throws StorageException {
        pipelineApi.deletePipeline(iri);
        assistantApi.deletePipeline(iri);
    }

    @Override
    public PipelineList createPipelineList() throws StorageException {
        return pipelineApi.createPipelineList();
    }

}
