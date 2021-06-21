package com.linkedpipes.etl.storage.template.store;

public class StoreInfo implements Cloneable {

    public int templateVersion = 0;

    public String repository = null;

    public StoreInfo clone() {
        StoreInfo result = new StoreInfo();
        result.templateVersion = templateVersion;
        result.repository = repository;
        return result;
    }

}
