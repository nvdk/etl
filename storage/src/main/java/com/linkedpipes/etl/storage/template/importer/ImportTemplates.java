package com.linkedpipes.etl.storage.template.importer;

import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

/**
 * Given pipeline definition import templates.
 */
public class ImportTemplates {

    private final TemplateEventListener listener;

    private final TemplateStore store;

    public ImportTemplates(
            TemplateEventListener listener, TemplateStore store) {
        this.listener = listener;
        this.store = store;
    }

    /**
     * Select and return all statements relevant for template definition.
     */
    public List<Statement> selectTemplatesData(List<Statement> statements) {
        return null;
    }

    /**
     * Given a pipeline import all new templates and update all
     * local templates.
     */
    public ImportResults importFromPipeline(List<Statement> statements) {
        return null;
    }

    /**
     * Given a pipeline consume
     */
    public ImportResults mapFromPipeline(List<Statement> statements) {
        return null;
    }


}
