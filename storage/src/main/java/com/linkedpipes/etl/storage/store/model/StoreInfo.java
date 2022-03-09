package com.linkedpipes.etl.storage.store.model;

public record StoreInfo(
        int version,
        String repository
) {

    public static final int CURRENT_VERSION = 5;

}
