package com.linkedpipes.etl.storage.template.store;

import com.linkedpipes.etl.storage.BaseException;

public class StoreException extends BaseException {

    public StoreException(String message, Object... args) {
        super(message, args);
    }

    public StoreException(Throwable cause) {
        super(cause);
    }

}
