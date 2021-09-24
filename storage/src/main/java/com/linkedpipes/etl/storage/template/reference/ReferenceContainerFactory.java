package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.TemplateException;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;

/**
 * Create new component from user provided input.
 */
public class ReferenceContainerFactory {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    /**
     * Return a container for a template of given IRI with given definition
     * and configuration. The resources in statements are updated to match
     * the given IRI.
     */
    public ReferenceContainer create(
            String iri,
            Collection<Statement> definitionStatements,
            Collection<Statement> configurationStatements)
            throws TemplateException {
        ReferenceDefinition definition =
                ReferenceDefinitionAdapter.create(definitionStatements);
        if (definition == null) {
            throw new TemplateException("Missing reference template type.");
        }
        return create(iri, definition, configurationStatements);
    }

    public ReferenceContainer create(
            String iri,
            ReferenceDefinition definition,
            Collection<Statement> configurationStatements) {
        definition.resource = valueFactory.createIRI(iri);
        definition.configurationGraph = createConfigurationIri(definition);
        //
        ReferenceContainer result = new ReferenceContainer();
        result.resource = definition.resource;
        result.definition = definition;
        result.definitionStatements = ReferenceDefinitionAdapter
                .asStatements(definition).withGraph(iri);
        result.configurationStatements =
                prepareConfiguration(definition, configurationStatements);
        return result;
    }

    protected Statements prepareConfiguration(
            ReferenceDefinition definition,
            Collection<Statement> configurationStatements) {
        IRI resource = createConfigurationIri(definition);
        if (configurationStatements == null
                || configurationStatements.isEmpty()) {
            return Statements.arrayList(0);
        }
        return Statements.wrap(RdfUtils.updateToIriAndGraph(
                configurationStatements, resource));
    }

    protected IRI createConfigurationIri(
            ReferenceDefinition definition) {
        String iri = definition.resource.stringValue() + "/configuration";
        return valueFactory.createIRI(iri);
    }

}
