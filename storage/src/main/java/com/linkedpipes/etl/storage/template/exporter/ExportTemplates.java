package com.linkedpipes.etl.storage.template.exporter;

import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.utils.Statements;

import java.util.Collection;
import java.util.List;

public class ExportTemplates {

    private final TemplateStore store;

    public ExportTemplates(TemplateStore store) {
        this.store = store;
    }

    /**
     * Export definition and configuration of given templates. Ignore
     * any redundant templates as well as
     * {@link com.linkedpipes.etl.storage.template.plugin.PluginTemplate}.
     */
    public List<Statements> exportTemplates(Collection<Template> templates) {
        return null;
    }

}
