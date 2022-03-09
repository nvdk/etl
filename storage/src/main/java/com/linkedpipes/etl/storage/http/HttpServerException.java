package com.linkedpipes.etl.storage.http;

import com.linkedpipes.etl.storage.StorageException;

/**
 * Error specific to the HTTP service.
 */
public class HttpServerException extends StorageException {

    public HttpServerException(String message, Object... args) {
        super(message, args);
    }

}
