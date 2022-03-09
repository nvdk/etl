package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.UpdateResources;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;
import java.util.Optional;

public class ReferenceTemplateService implements ReferenceTemplateApi {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final ReferenceTemplateStore store;

    public ReferenceTemplateService(ReferenceTemplateStore store) {
        this.store = store;
    }

    @Override
    public Collection<String> listReferenceTemplates()
            throws StorageException {
        return store.listReferencesTemplate();
    }

    @Override
    public Optional<ReferenceTemplate> loadReferenceTemplate(String iri)
            throws StorageException {
        return store.loadReferenceTemplate(iri);
    }

    @Override
    public ReferenceTemplate storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException {
        ReferenceTemplate instance;
        if (template.resource() == null || template.resource().isBNode()) {
            Resource resource = valueFactory.createIRI(
                    store.reserveReferenceIri());
            instance = updateTemplateResource(template, resource);
        } else {
            instance = new ReferenceTemplate(template);
        }
        store.storeReferenceTemplate(instance);
        return instance;
    }

    private ReferenceTemplate updateTemplateResource(
            ReferenceTemplate template, Resource resource) {
        return new ReferenceTemplate(
                resource,
                template.template(),
                template.prefLabel(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                template.knownAs(),
                template.pluginTemplate(),
                template.version(),
                UpdateResources.apply(
                        resource.stringValue() + "/configuration/",
                        template.configuration()),
                ReferenceTemplate.defaultConfigurationGraph(resource));
    }

    @Override
    public void deleteReferenceTemplate(String iri)
            throws StorageException {
        store.deleteReferenceTemplate(iri);
    }

}
