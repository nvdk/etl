package com.linkedpipes.etl.executor.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ExecutionProfile implements Loadable {

    private String rdfRepositoryPolicy = LP_PIPELINE.SINGLE_REPOSITORY;

    private String rdfRepositoryType = LP_PIPELINE.NATIVE_STORE;

    @Override
    public Loadable load(String predicate, BackendRdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY:
                rdfRepositoryPolicy = value.asString();
                return null;
            case LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE:
                rdfRepositoryType = value.asString();
                return null;
            default:
                return null;
        }
    }

    public String getRdfRepositoryPolicy() {
        return rdfRepositoryPolicy;
    }

    public String getRdfRepositoryType() {
        return rdfRepositoryType;
    }

}
