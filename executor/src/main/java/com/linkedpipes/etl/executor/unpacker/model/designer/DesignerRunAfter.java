package com.linkedpipes.etl.executor.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class DesignerRunAfter implements Loadable {

    public static final String TYPE = LP_PIPELINE.RUN_AFTER;

    private String iri;

    private String sourceComponent;

    private String targetComponent;

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                sourceComponent = value.asString();
                return null;
            case LP_PIPELINE.HAS_TARGET_COMPONENT:
                targetComponent = value.asString();
                return null;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public String getTargetComponent() {
        return targetComponent;
    }

}

