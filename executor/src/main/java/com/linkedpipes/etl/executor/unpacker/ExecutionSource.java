package com.linkedpipes.etl.executor.unpacker;

import com.linkedpipes.etl.executor.ExecutorException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

public interface ExecutionSource {

    Collection<Statement> getExecution(String executionIri)
            throws ExecutorException;

}
