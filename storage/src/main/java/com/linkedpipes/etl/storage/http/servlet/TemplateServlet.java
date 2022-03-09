package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.http.ContentReader;
import com.linkedpipes.etl.storage.http.ExceptionHandlerWrap;
import com.linkedpipes.etl.storage.rdf.Statements;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.io.InputStream;

@Path("/template")
public class TemplateServlet {

    @Inject
    public TemplateServletService service;

    /**
     * Return list of all stored templates, include plugin and reference
     * templates.
     */
    @GET
    @Path("/list")
    public Response getTemplates(@Context HttpServletRequest request) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.listTemplates(request));
    }

    /**
     * Return definition of given template.
     */
    @GET
    @Path("/definition")
    public Response getDefinition(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.templateDefinition(request, iri));
    }

    /**
     * Return configuration description for given template. This called
     * be called for non-plugin templates.
     */
    @GET
    @Path("/configuration-description")
    public Response getConfigurationDescription(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.configurationDescription(request, iri));
    }

    /**
     * Return stored configuration for given template.
     */
    @GET
    @Path("/stored-configuration")
    public Response getConfiguration(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.configuration(request, iri));
    }

    /**
     * Return configuration that should be used for new children
     * of the template.
     */
    @GET
    @Path("/new-configuration")
    public Response getNewConfiguration(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.newConfiguration(request, iri));
    }

    /**
     * For given component return effective configuration, i.e.
     * configuration as created after parent configuration were applied.
     * This configuration will be used during the execution.
     */
    @GET
    @Path("/effective-configuration")
    public Response getEffectiveConfiguration(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.effectiveConfiguration(request, iri));
    }

    /**
     * Return file for given component, this can be called on
     * non-plugin component as well.
     */
    @GET
    @Path("/file")
    public Response getTemplateFile(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri,
            @QueryParam("path") String path) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.templateFile(request, iri, path));
    }

    /**
     * Store a component. If component resource is not set,
     * create a new component from given definition.
     */
    @POST
    public Response postTemplate(
            @Context HttpServletRequest request,
            InputStream templateStream) {
        return ExceptionHandlerWrap.wrap(request, () -> {
            Statements componentRdf = ContentReader.readHttpStream(
                    templateStream, request.getHeader("Content-Type"));
            return service.storeTemplate(request, componentRdf.selector());
        });
    }

    /**
     * Remove given component.
     */
    @DELETE
    public Response deleteTemplate(
            @Context HttpServletRequest request,
            @QueryParam("iri") String iri) {
        return ExceptionHandlerWrap.wrap(
                request, () -> service.deleteTemplate(iri));
    }

}
