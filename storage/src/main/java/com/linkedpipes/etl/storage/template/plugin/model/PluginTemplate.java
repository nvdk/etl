package com.linkedpipes.etl.storage.template.plugin.model;

import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.storage.rdf.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collections;
import java.util.List;

/**
 * Full definition of a plugin template data structure.
 */
public record PluginTemplate(
        Resource resource,
        Value prefLabel,
        Value color,
        IRI type,
        Literal supportControl,
        List<Value> tags,
        IRI infoLink,
        List<String> dialogs,
        List<Port> ports,
        Resource jarResource,
        @Deprecated
        List<IRI> requirement,
        /*
         * Configuration statements without graph.
         */
        Statements configuration,
        Resource configurationGraph,
        /*
         * Graph is set even if no configuration is given.
         */
        ConfigurationDescription configurationDescription,
        /*
         * Graph is set even if no description is given.
         */
        Resource configurationDescriptionGraph
) {

    public record Port(
            Value binding,
            Value prefLabel,
            List<IRI> types,
            @Deprecated
            List<IRI> requirement
    ) {

    }

    public List<Value> tags() {
        return Collections.unmodifiableList(tags);
    }

    public List<String> dialogs() {
        return Collections.unmodifiableList(dialogs);
    }

    public List<Port> ports() {
        return Collections.unmodifiableList(ports);
    }

    public List<IRI> requirement() {
        return Collections.unmodifiableList(requirement);
    }

    public static IRI defaultConfigurationGraph(Resource resource) {
        return SimpleValueFactory.getInstance().createIRI(
                resource.stringValue() + "/configuration");
    }

    public static IRI defaultConfigurationDescriptionGraph(Resource resource) {
        return SimpleValueFactory.getInstance().createIRI(
                resource.stringValue() + "/configuration-description");
    }

}
