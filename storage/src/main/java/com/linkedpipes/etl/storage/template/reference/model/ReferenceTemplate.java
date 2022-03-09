package com.linkedpipes.etl.storage.template.reference.model;

import com.linkedpipes.etl.storage.rdf.Statements;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collections;
import java.util.List;

/**
 * Full definition of a reference template data structure.
 */
public record ReferenceTemplate(
        Resource resource,
        Resource template,
        Literal prefLabel,
        Literal description,
        Literal note,
        Value color,
        /*
         * First IRI assigned to other template, used to map template between
         * instances.
         */
        List<Literal> tags,
        Resource knownAs,
        /*
         * IRI of the plugin template, i.e. the plugin template.
         */
        Resource pluginTemplate,
        Literal version,
        Statements configuration,
        Resource configurationGraph
) {

    private static final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    /**
     * Create a copy of given template.
     */
    public ReferenceTemplate(ReferenceTemplate other) {
        this(
                other.resource,
                other.template,
                other.prefLabel,
                other.description,
                other.note,
                other.color,
                other.tags,
                other.knownAs,
                other.pluginTemplate,
                other.version,
                other.configuration,
                other.configurationGraph

        );
    }

    public List<Literal> tags() {
        return Collections.unmodifiableList(tags);
    }

    public Statements configuration() {
        return Statements.readOnly(configuration);
    }

    public static Resource defaultConfigurationGraph(Resource resource) {
        if (resource == null || resource.isBNode()) {
            return null;
        }
        String iriAsString = resource.stringValue() + "/configuration";
        return valueFactory.createIRI(iriAsString);
    }

}
