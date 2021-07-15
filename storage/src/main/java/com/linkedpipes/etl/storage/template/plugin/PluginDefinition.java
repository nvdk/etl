package com.linkedpipes.etl.storage.template.plugin;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Full definition of a plugin template data structure.
 */
public class PluginDefinition {

    static class Port {

        public Value binding;

        public Value prefLabel;

        public List<Resource> types = new ArrayList<>(2);

        @Deprecated
        public List<Value> requirement = new ArrayList<>(2);

    }

    public Resource resource;

    public Value prefLabel;

    public Value color;

    public IRI type;

    public Value supportControl;

    public List<Value> tags = new ArrayList<>(4);

    public IRI infoLink;

    public Resource configurationGraph;

    public Resource configurationDescriptionGraph;

    public List<String> dialogs = new ArrayList<>(4);

    public List<Port> ports = new ArrayList<>(4);

    public Resource jarResource;

    @Deprecated
    public List<Value> requirement = new ArrayList<>(2);

}
