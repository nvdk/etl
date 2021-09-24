package com.linkedpipes.etl.storage.template.importer;

import com.linkedpipes.etl.storage.BaseException;

public class TemplateImportException extends BaseException {

    public TemplateImportException(String message) {
        super(message);
    }

    public TemplateImportException(String message, Throwable cause) {
        super(message, cause);
    }

}
