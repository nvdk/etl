package com.linkedpipes.etl.storage.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandlerWrap {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExceptionHandlerWrap.class);

    @FunctionalInterface
    public interface Handler {

        Response call() throws Throwable;

    }

    public static Response wrap(HttpServletRequest request, Handler handler) {
        try {
            return handler.call();
        } catch(Throwable t) {
            LOG.error("Request {} : {} failed.",
                    request.getMethod(), request.getRequestURI(), t);
            return Response.serverError().build();
        }
    }

}
