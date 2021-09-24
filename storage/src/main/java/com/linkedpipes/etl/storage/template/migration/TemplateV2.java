package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.utils.Statements;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class TemplateV2 {

    private static final String HAS_CONFIGURATION_DESCRIPTION =
            "http://linkedpipes.com/ontology/configurationDescription";

    /**
     * Remove configuration description and reference to it.
     * <p>
     * This used to also delete the configuration description file, but we do
     * not need that functionality now.
     */
    public ReferenceContainer migrateToV3(ReferenceContainer template) {
        return new ReferenceContainer(
                template.resource,
                Statements.wrap(removeConfigDescriptionReference(
                        template.definitionStatements)),
                template.configurationStatements, null);
    }

    protected List<Statement> removeConfigDescriptionReference(
            Collection<Statement> statements) {
        return statements.stream().filter(
                (st) -> {
                    String predicate = st.getPredicate().stringValue();
                    return !HAS_CONFIGURATION_DESCRIPTION.equals(predicate);
                }).collect(Collectors.toList());
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
