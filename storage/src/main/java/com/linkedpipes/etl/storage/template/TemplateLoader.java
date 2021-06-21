package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
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

    public Template loadTemplate(RepositoryReference reference)
            throws BaseException {
        switch (reference.getType()) {
            case JAR_TEMPLATE:
                return loadJarTemplate(reference);
            case REFERENCE_TEMPLATE:
                return loadReferenceTemplate(reference);
            default:
                throw new BaseException("No template find for: {}",
                        reference.getId());
        }
    }

    public JarTemplate loadJarTemplate(
            RepositoryReference reference) throws BaseException {
        Collection<Statement> definition =
                store.getDefinition(reference);
        JarTemplate template = new JarTemplate();
        template.setId(reference.getId());
        PojoLoader.loadOfType(definition, JarTemplate.TYPE, template);
        return template;
    }

    public ReferenceTemplate loadReferenceTemplate(
            RepositoryReference reference) throws BaseException {
        Collection<Statement> definition =
                store.getDefinition(reference);
        ReferenceTemplate template = new ReferenceTemplate();
        template.setId(reference.getId());
        PojoLoader.loadOfType(definition, ReferenceTemplate.TYPE, template);
        return template;
    }

}
