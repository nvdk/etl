package com.linkedpipes.etl.storage.template.store;

import com.linkedpipes.etl.storage.template.store.cached.CachedStoreV1;

public class StoreInfo implements Cloneable {

    public static final int LATEST_TEMPLATE_VERSION = 5;

    public int templateVersion = 0;

    public String repository = null;

    public static StoreInfo CachedFileStore() {
        StoreInfo result = new StoreInfo();
        result.templateVersion = LATEST_TEMPLATE_VERSION;
        result.repository = CachedStoreV1.STORE_NAME;
        return result;
    }

}
