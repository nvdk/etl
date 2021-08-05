package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.migration.MigrateStore;
import com.linkedpipes.etl.storage.template.plugin.LoadPluginTemplates;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.LoadReferenceTemplates;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinitionAdapter;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainerFactory;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

class TemplateService {

    private final Configuration configuration;

    private final TemplateEventListener listener;

    private TemplateStore store;

    public TemplateService(
            Configuration configuration,
            TemplateEventListener listener) {
        this.configuration = configuration;
        this.listener = listener;
    }

    public void initialize() throws BaseException {
        store = LoadTemplates.apply(configuration, listener);
    }

    public List<Statement> getDefinition(Template template)
            throws BaseException {
        if (template.isPluginTemplate()) {
            return store.getPluginDefinition(template.getId());
        } else if (template.isReferenceTemplate()) {
            return store.getReferenceDefinition(template.getId());
        } else {
            throw new BaseException("Unknown template: {}", template.getId());
        }
    }

    public List<Statement> getConfiguration(Template template)
            throws BaseException {
        if (template.isPluginTemplate()) {
            return store.getPluginConfiguration(template.getId());
        } else if (template.isReferenceTemplate()) {
            return store.getReferenceConfiguration(template.getId());
        } else {
            throw new BaseException("Unknown template type: {}",
                    template.getId());
        }
    }

    public List<Statement> getConfigurationDescription(PluginTemplate template)
            throws BaseException {
        return store.getPluginConfigurationDescription(template.id);
    }

    public byte[] getPluginFile(Template template, String path)
            throws StoreException {
        return store.getPluginFile(template.getId(), path);
    }

    public Template createReferenceTemplate(
            Collection<Statement> definitionStatements,
            Collection<Statement> configurationStatements)
            throws BaseException {
        String id = store.reserveIdentifier();
        String iri = configuration.getDomainName()
                + "/resources/components/" + id;
        ReferenceContainerFactory factory = new ReferenceContainerFactory();
        try {
            ReferenceContainer container = factory.create(
                    id, iri, definitionStatements, configurationStatements);
            ReferenceTemplate referenceTemplate =
                    new ReferenceTemplate(container);
            referenceTemplate.setCorePlugin(
                    referenceTemplate.getCorePlugin());
            listener.onReferenceTemplateLoaded(container);
            ;
            return referenceTemplate;
        } catch (BaseException ex) {
            store.removeReference(id);
            throw ex;
        }
    }

    public void updateReferenceTemplate(
            ReferenceTemplate template,
            Collection<Statement> definitionStatements)
            throws BaseException {
        String id = template.getId();
        ReferenceDefinition definition = ReferenceDefinitionAdapter.create(
                store.getReferenceDefinition(id));
        ReferenceDefinition givenDefinition =
                ReferenceDefinitionAdapter.create(definitionStatements);
        ReferenceDefinition newDefinition =
                updateReferenceTemplateDefinition(definition, givenDefinition);
        store.setReferenceDefinition(
                id, ReferenceDefinitionAdapter.asStatements(definition));
        listener.onReferenceTemplateChanged(definition, newDefinition);
    }

    /**
     * Merge definition, by doing so specify what can be updated.
     */
    protected ReferenceDefinition updateReferenceTemplateDefinition(
            ReferenceDefinition oldDefinition,
            ReferenceDefinition newDefinition) {
        ReferenceDefinition result = new ReferenceDefinition();
        result.resource = oldDefinition.resource;
        result.template = oldDefinition.template;
        result.version = oldDefinition.version;
        result.root = oldDefinition.root;
        result.knownAs = oldDefinition.knownAs;
        result.configurationGraph = oldDefinition.configurationGraph;
        //
        result.prefLabel = newDefinition.prefLabel;
        result.description = newDefinition.description;
        result.note = newDefinition.note;
        result.color = newDefinition.color;
        result.tags = newDefinition.tags;
        return result;
    }

    public void updateReferenceConfiguration(
            Template template, Collection<Statement> statements)
            throws BaseException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graph = valueFactory.createIRI(
                template.getIri() + "/configuration");
        statements = RdfUtils.forceContext(statements, graph);
        store.setReferenceConfiguration(template.getId(), statements);
        listener.onReferenceTemplateConfigurationChanged(
                template.getIri(), statements);
    }

    public void removeReference(ReferenceTemplate template)
            throws BaseException {
        listener.onReferenceTemplateDeleted(template.getIri());
        store.removeReference(template.getId());
    }

}
