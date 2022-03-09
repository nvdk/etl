package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.rdf.Statements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

abstract class ServletService {

    protected ServletService() {
    }

    protected Response responseStatements(
            HttpServletRequest request, Statements statements) {
        RDFFormat format = getResponseFormat(request);
        Response.ResponseBuilder result = prepareResponse(format);

        StreamingOutput output = (stream) -> {
            statements.file().writeToStream(stream, format);
        };

        return result.entity(output).build();
    }

    protected RDFFormat getResponseFormat(HttpServletRequest request) {
        return Rio.getParserFormatForMIMEType(request.getHeader("Accept"))
                .orElse(RDFFormat.TRIG);
    }

    protected Response.ResponseBuilder prepareResponse(RDFFormat format) {
        Response.ResponseBuilder result = Response.ok();
        result.header("content-type", format.getDefaultMIMEType());
        return result;
    }

    protected Response responseNotFound() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}
