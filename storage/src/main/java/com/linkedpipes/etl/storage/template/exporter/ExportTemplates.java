package com.linkedpipes.etl.storage.template.exporter;

import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<Statement> exportTemplates(
            Collection<Template> templates) throws StoreException {
        Set<String> exported = new HashSet<>();
        List<Statement> result = new ArrayList<>();
        for (Template template : templates) {
            String iri = template.getIri();
            if (exported.contains(iri)) {
                continue;
            }
            exported.add(iri);
            if (template.isReferenceTemplate()) {
                result.addAll(store.getReferenceDefinition(iri));
                result.addAll(store.getReferenceConfiguration(iri));
            }
        }
        return result;
    }

}
