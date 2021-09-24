package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class TemplateV4 {

    protected final MigrationSources.RootSource rootSource;

    protected final MigrationSources.MappingSource mappingSource;

    public TemplateV4(MigrationSources.RootSource rootSource,
                      MigrationSources.MappingSource mappingSource) {
        this.rootSource = rootSource;
        this.mappingSource = mappingSource;
    }

    /**
     * Add version information, mapping and core component. Those changes
     * make reference template definition self contained and allow
     * for per template migration in the future.
     */
    public ReferenceContainer migrateToV5(ReferenceContainer template)
            throws BaseException {
        return new ReferenceContainer(
                template.resource,
                updateDefinition(
                        template.definitionStatements,
                        template.resource.stringValue()),
                template.configurationStatements,
                null);
    }

    protected Statements updateDefinition(
            Statements statements, String iri) throws BaseException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        statements.setDefaultGraph(valueFactory.createIRI(iri));
        statements.addIri(
                iri, LP_PIPELINE.HAS_ROOT, rootSource.getRoot(iri));
        String mapping = mappingSource.getMapping(iri);
        if (mapping != null) {
            statements.addIri(
                    iri, LP_PIPELINE.HAS_KNOWN_AS,
                    mappingSource.getMapping(iri));
        }
        statements.addInt(iri, LP.HAS_VERSION, 5);
        return statements;
    }

    public static Resource loadParent(
            Resource resource, Statements statements) {
        return TemplateV0.loadParent(resource, statements);
    }

    public static Resource loadConfiguration(
            Resource resource, Statements statements) {
        return TemplateV0.loadConfiguration(resource, statements);
    }

}
