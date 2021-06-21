package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;

public interface TemplatesInformation {

    String getRoot(String identifier) throws BaseException;

}
