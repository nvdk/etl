package com.linkedpipes.etl.executor.executions;

import com.linkedpipes.etl.executor.unpacker.ExecutionSource;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class ExecutionFacade implements ExecutionSource {

    @Override
    public Collection<Statement> getExecution(String iri) {
        return Collections.emptyList();
    }

}
