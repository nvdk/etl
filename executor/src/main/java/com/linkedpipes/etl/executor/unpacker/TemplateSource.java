package com.linkedpipes.etl.executor.unpacker;

import com.linkedpipes.etl.executor.ExecutorException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;


public interface TemplateSource {

    Collection<Statement> getDefinition(String iri) throws ExecutorException;

    /**
     * Get template configuration.
     */
    Collection<Statement> getConfiguration(String iri) throws ExecutorException;

    Collection<Statement> getConfigurationDescription(String iri)
            throws ExecutorException;

}
