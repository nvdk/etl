package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.assistant.AssistantApi;
import com.linkedpipes.etl.storage.assistant.AssistantData;
import com.linkedpipes.etl.storage.assistant.AssistantDataAdapter;
import com.linkedpipes.etl.storage.assistant.PipelineIriList;
import com.linkedpipes.etl.storage.rdf.Statements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

public class AssistantServletService extends ServletService {

    private final AssistantApi assistantApi;

    public AssistantServletService(AssistantApi assistantApi) {
        this.assistantApi = assistantApi;
    }

    public Response usage(HttpServletRequest request, String iri)
            throws StorageException {
        PipelineIriList pipelines = assistantApi.pipelinesWithTemplate(iri);
        Statements content = AssistantDataAdapter.asRdf(pipelines);
        return responseStatements(request, content);
    }

    public Response designData(HttpServletRequest request) {
        AssistantData data = assistantApi.getData();
        Statements content = AssistantDataAdapter.asRdf(data);
        return responseStatements(request, content);
    }

}
