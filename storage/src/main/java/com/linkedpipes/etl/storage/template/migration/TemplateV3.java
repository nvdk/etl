package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.utils.Statements;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

class TemplateV3 {

    private static final String HAS_CONFIGURATION_GRAPH =
            "http://linkedpipes.com/ontology/configurationGraph";

    /**
     * Add reference to the configuration graph.
     */
    public ReferenceContainer migrateToV4(ReferenceContainer template) {
        return new ReferenceContainer(
                template.resource,
                addConfigurationIri(
                        template.definitionStatements,
                        template.resource.stringValue()),
                template.configurationStatements,
                null);
    }

    protected Statements addConfigurationIri(
            Statements statements, String iri) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI predicate = valueFactory.createIRI(HAS_CONFIGURATION_GRAPH);
        // It seems that sometimes the version was stored, so this is
        // to prevent duplicities.
        statements.removeIf((st) -> predicate.equals(st.getPredicate()));
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(iri),
                predicate,
                valueFactory.createIRI(createConfigurationIri(iri)),
                valueFactory.createIRI(iri)
        ));
        return statements;
    }

    public static String createConfigurationIri(String templateIri) {
        return templateIri + "/configuration";
    }

    public static Resource loadParent(
            Resource resource, Statements statements) {
        return TemplateV0.loadParent(resource, statements);
    }

    public static Integer loadVersion(
            Resource resource, Statements statements) {
        // There was no information about version.
        return null;
    }

    public static Resource loadConfiguration(
            Resource resource, Statements statements) {
        return TemplateV0.loadConfiguration(resource, statements);
    }

}
