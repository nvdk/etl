package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;

public class TemplateException extends BaseException {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

}
