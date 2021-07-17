package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
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

    public ReferenceContainer create(
            String id, String iri,
            Collection<Statement> definitionStatements,
            Collection<Statement> configurationStatements)
            throws BaseException {
        ReferenceDefinition definition =
                ReferenceDefinitionAdapter.create(definitionStatements);
        if (definition == null) {
            throw new BaseException("Missing reference template type.");
        }
        definition.resource = valueFactory.createIRI(iri);


        //
        ReferenceContainer result = new ReferenceContainer();
        result.identifier = id;
        result.resource = definition.resource;
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
