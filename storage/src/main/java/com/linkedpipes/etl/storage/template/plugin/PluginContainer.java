package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Keep all data about plugin template  in a single structure for an easier
 * manipulation.
 */
public class PluginContainer {

    public String identifier;

    public Resource resource;

    public Statements definitionStatements;

    public Statements configurationStatements;

    public Statements configurationDescriptionStatements;

    public Map<String, byte[]> files = new HashMap<>();

    public PluginDefinition definition;

}
