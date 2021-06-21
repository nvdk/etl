package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;

public class TemplateV4 {

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
