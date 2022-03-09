package com.linkedpipes.etl.executor.unpacker.model.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

import java.util.ArrayList;
import java.util.List;

public class ExecutionComponent implements Loadable {

    private String iri;

    private List<ExecutionPort> ports = new ArrayList<>();

    private String execution;

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue value) {
        switch (predicate) {
            case LP_EXEC.HAS_DATA_UNIT:
                ExecutionPort newPort = new ExecutionPort();
                ports.add(newPort);
                return newPort;
            case LP_EXEC.HAS_EXECUTION:
                execution = value.asString();
                return null;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public ExecutionPort getPortByBinding(String binding) {
        for (ExecutionPort port : ports) {
            if (port.getBinding().equals(binding)) {
                return port;
            }
        }
        return null;
    }

    public String getExecution() {
        return execution;
    }

}
