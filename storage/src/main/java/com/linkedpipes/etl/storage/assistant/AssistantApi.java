package com.linkedpipes.etl.storage.assistant;

public interface AssistantApi {

    AssistantData getData();

    PipelineIriList pipelinesWithTemplate(String template);

}
