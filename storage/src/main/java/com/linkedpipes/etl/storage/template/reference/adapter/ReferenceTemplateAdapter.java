package com.linkedpipes.etl.storage.template.reference.adapter;

import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.reference.adapter.rdf.RdfToReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.adapter.rdf.ReferenceTemplateToRdf;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

import java.util.List;

public class ReferenceTemplateAdapter {

    public static List<ReferenceTemplate> asReferenceTemplates(
            Statements statements) {
        return RdfToReferenceTemplate.asReferenceTemplates(
                statements.selector());
    }

    public static Statements definitionAsRdf(ReferenceTemplate definition) {
        return ReferenceTemplateToRdf.definitionAsRdf(definition);
    }

    public static Statements configurationAsRdf(ReferenceTemplate definition) {
        return ReferenceTemplateToRdf.configurationAsRdf(definition);
    }

    public static Statements asRdf(ReferenceTemplate definition) {
        Statements result = Statements.arrayList();
        result.addAll(definitionAsRdf(definition));
        result.addAll(configurationAsRdf(definition));
        return result;
    }

}
