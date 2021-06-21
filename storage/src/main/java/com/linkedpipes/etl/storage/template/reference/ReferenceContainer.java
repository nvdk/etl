package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds reference template data in memory.
 */
public class ReferenceContainer {

    public Resource resource;

    public Statements definition;

    public Statements configuration;

    public ReferenceContainer() {
    }

    public ReferenceContainer(
            Resource resource, Statements definition, Statements configuration
    ) {
        this.resource = resource;
        this.definition = definition;
        this.configuration = configuration;
    }

}
