package com.linkedpipes.etl.storage.template.reference.migration;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ReferenceTemplateV4 {

    private final PluginTemplateSource templateSource;

    public ReferenceTemplateV4(PluginTemplateSource templateSource) {
        this.templateSource = templateSource;
    }

    /**
     * Add version information, mapping and core component. Those changes
     * make reference template definition self-contained and allow
     * for per template migration in the future.
     */
    public ReferenceTemplate migrateToV5(ReferenceTemplate template)
            throws StorageException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        String rootTemplate = templateSource.getPluginTemplate(
                template.resource().stringValue());
        if (rootTemplate == null) {
            throw new StorageException(
                    "Missing root template '{}' for '{}'.",
                    template.template(),
                    template.resource());
        }
        // Mapping is not new information thus it is added, during
        // reading the information from RDF.
        return new ReferenceTemplate(
                template.resource(),
                template.template(),
                template.prefLabel(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                template.knownAs(),
                valueFactory.createIRI(rootTemplate),
                valueFactory.createLiteral(5),
                template.configuration(),
                template.configurationGraph());
    }

}
