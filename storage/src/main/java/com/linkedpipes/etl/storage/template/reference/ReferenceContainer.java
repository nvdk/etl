package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;

/**
 * Keep all data about reference in a single structure for an easier
 * manipulation.
 */
public class ReferenceContainer {

    public String identifier;

    public Resource resource;

    public Statements definitionStatements;

    public Statements configurationStatements;

    public ReferenceDefinition definition;

    public ReferenceContainer() {
    }

    public ReferenceContainer(
            Resource resource,
            Statements definitionStatements,
            Statements configurationStatements,
            ReferenceDefinition definition) {
        this.resource = resource;
        this.definitionStatements = definitionStatements;
        this.configurationStatements = configurationStatements;
        this.definition = definition;
    }

}
