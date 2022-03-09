package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.http.ExceptionHandlerWrap;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("assistant")
public class AssistantServlet {

    @Inject
    public AssistantServletService service;


    /**
     * Return all pipelines where the component is used.
     */
    @GET
    @Path("/template-usage")
    public Response getComponentUsage(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.usage(request, iri));
    }

    /**
     * Return design data about pipelines.
     */
    @GET
    @Path("/pipeline-statistics")
    public Response getDesignData(@Context HttpServletRequest request) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.designData(request));
    }

}
