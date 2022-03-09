package com.linkedpipes.etl.storage.store;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineStore;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateStore;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateStore;

public interface Store
        extends PluginTemplateStore, ReferenceTemplateStore, PipelineStore {

    String getName();

    default void initialize() throws StorageException {
        // Do nothing.
    }

}
