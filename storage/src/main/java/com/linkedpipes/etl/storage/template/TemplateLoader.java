package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

/**
 * Load templates into {@link TemplateManager}.
 */
class TemplateLoader {

    private final TemplateStore store;

    public TemplateLoader(TemplateStore store) {
        this.store = store;
    }

    public PluginTemplate loadPluginTemplate(String id)
            throws BaseException {
        Collection<Statement> definition =
                store.getPluginDefinition(id);
        PluginTemplate template = new PluginTemplate();
        template.setId(id);
        PojoLoader.loadOfType(definition, PluginTemplate.TYPE, template);
        return template;
    }

    public ReferenceTemplate loadReferenceTemplate(String id)
            throws BaseException {
        Collection<Statement> definition = store.getReferenceDefinition(id);
        ReferenceTemplate template = new ReferenceTemplate();
        template.setId(id);
        PojoLoader.loadOfType(definition, ReferenceTemplate.TYPE, template);
        return template;
    }

}
