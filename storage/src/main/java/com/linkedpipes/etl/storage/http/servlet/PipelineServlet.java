package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.http.ContentReader;
import com.linkedpipes.etl.storage.http.ExceptionHandlerWrap;
import com.linkedpipes.etl.storage.rdf.Statements;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.io.InputStream;

@Path("/pipeline")
public class PipelineServlet {

    @Inject
    public PipelineServletService service;

    @GET
    @Path("/list")
    public Response getPipelines(@Context HttpServletRequest request) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.listPipelines(request));
    }

    /**
     * Use "removePrivateConfiguration" in query part to remove private
     * configuration.
     */
    @GET
    @Path("/definition")
    public Response getPipeline(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.pipeline(request, iri));
    }

    /**
     * Use "removePrivateConfiguration" in query part to remove private
     * configuration.
     */
    @GET
    @Path("/full")
    public Response getFullPipeline(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.fullPipeline(request, iri));
    }

    /**
     * Use "removePrivateConfiguration" in query part to remove private
     * configuration.
     */
    @GET
    @Path("/squashed")
    public Response getSquashedPipeline(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.squashedPipeline(request, iri));
    }

    @POST
    public Response postPipeline(
            @Context HttpServletRequest request,
            InputStream pipelineStream) {
        return ExceptionHandlerWrap.wrap(request, () -> {
            Statements pipelineRdf = ContentReader.readHttpStream(
                    pipelineStream, request.getHeader("Content-Type"));
            return service.storePipeline(pipelineRdf.selector());
        });
    }

    @POST
    @Path("/localize")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postPipelineLocalize(
            @Context HttpServletRequest request,
            @FormDataParam("pipeline") InputStream pipelineStream,
            @FormDataParam("pipeline") FormDataContentDisposition pipelineForm,
            @FormDataParam("option") InputStream optionStream,
            @FormDataParam("option") FormDataContentDisposition optionForm) {
        return ExceptionHandlerWrap.wrap(request, () -> {
            Statements pipelineRdf = ContentReader.readFileStream(
                    pipelineStream, pipelineForm.getFileName());
            Statements optionRdf = ContentReader.readFileStream(
                    optionStream, optionForm.getFileName());
            return service.localizePipeline(pipelineRdf, optionRdf, request);
        });
    }

    /**
     * Store or create a new pipeline(s).
     */
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postPipelineMultipart(
            @Context HttpServletRequest request,
            @FormDataParam("pipeline") InputStream pipelineStream,
            @FormDataParam("pipeline") FormDataContentDisposition pipelineForm,
            @FormDataParam("option") InputStream optionStream,
            @FormDataParam("option") FormDataContentDisposition optionForm) {
        return ExceptionHandlerWrap.wrap(request, () -> {
            Statements pipelineRdf = ContentReader.readFileStream(
                    pipelineStream, pipelineForm.getFileName());
            Statements optionRdf = ContentReader.readFileStream(
                    optionStream, optionForm.getFileName());
            return service.importPipeline(pipelineRdf, optionRdf, request);
        });
    }

    /**
     * Remove given pipeline.
     */
    @DELETE
    public Response deletePipeline(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.deletePipeline(iri));
    }

}
