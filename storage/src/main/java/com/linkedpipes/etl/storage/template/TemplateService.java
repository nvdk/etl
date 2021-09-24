package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinitionAdapter;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainerFactory;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
            return store.getPluginDefinition(template.getIri());
        } else if (template.isReferenceTemplate()) {
            return store.getReferenceDefinition(template.getIri());
        } else {
            throw new BaseException("Unknown template: {}", template.getIri());
        }
    }

    public List<Statement> getConfiguration(Template template)
            throws BaseException {
        if (template.isPluginTemplate()) {
            return store.getPluginConfiguration(template.getIri());
        } else if (template.isReferenceTemplate()) {
            return store.getReferenceConfiguration(template.getIri());
        } else {
            throw new BaseException("Unknown template type: {}",
                    template.getIri());
        }
    }

    public List<Statement> getConfigurationDescription(PluginTemplate template)
            throws BaseException {
        return store.getPluginConfigurationDescription(template.getIri());
    }

    public byte[] getPluginFile(Template template, String path)
            throws StoreException {
        return store.getPluginFile(template.getIri(), path);
    }

    public Template createReferenceTemplate(
            Collection<Statement> definitionStatements,
            Collection<Statement> configurationStatements)
            throws BaseException {
        String iri = store.reserveIri(configuration.getDomainName());
        ReferenceContainerFactory factory =
                new ReferenceContainerFactory();
        try {
            ReferenceContainer container = factory.create(
                    iri, definitionStatements, configurationStatements);
            ReferenceTemplate referenceTemplate =
                    new ReferenceTemplate(container);
            referenceTemplate.setRootPluginTemplate(
                    referenceTemplate.getRootPluginTemplate());
            // TODO Add validation of the user provided input.
            store.setReference(iri,
                    container.definitionStatements,
                    container.configurationStatements);
            listener.onReferenceTemplateLoaded(container);
            return referenceTemplate;
        } catch (BaseException ex) {
            store.removeReference(iri);
            throw ex;
        }
    }

    public void updateReferenceTemplate(
            ReferenceTemplate template,
            Collection<Statement> definitionStatements)
            throws BaseException {
        String id = template.getIri();
        ReferenceDefinition oldDefinition = ReferenceDefinitionAdapter.create(
                store.getReferenceDefinition(id));
        if (oldDefinition == null) {
            throw new TemplateException("Can't load old definition.");
        }
        ReferenceDefinition givenDefinition =
                ReferenceDefinitionAdapter.create(definitionStatements);
        if (givenDefinition == null) {
            throw new TemplateException("Can't load given definition.");
        }
        ReferenceDefinition newDefinition =
                updateReferenceTemplateDefinition(
                        oldDefinition, givenDefinition);
        store.setReferenceDefinition(
                id, ReferenceDefinitionAdapter.asStatements(oldDefinition));
        listener.onReferenceTemplateChanged(oldDefinition, newDefinition);
    }

    /**
     * Merge definition, by doing so specify what can be updated. Also makes
     * sure that invalid new definition can not break the old one as all the
     * core attributes are loaded from the old version.
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
        store.setReferenceConfiguration(template.getIri(), statements);
        listener.onReferenceTemplateConfigurationChanged(
                template.getIri(), statements);
    }

    public void removeReference(ReferenceTemplate template)
            throws BaseException {
        listener.onReferenceTemplateDeleted(template.getIri());
        store.removeReference(template.getIri());
    }

}
