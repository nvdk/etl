package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoadReferenceTemplates {

    private static final Logger LOG =
            LoggerFactory.getLogger(LoadReferenceTemplates.class);

    public static void apply(
            TemplateEventListener listener,
            TemplateStore store)
            throws StoreException {
        (new LoadReferenceTemplates(listener, store))
                .loadReferenceTemplates();
    }

    private final TemplateEventListener listener;

    private final TemplateStore store;

    protected LoadReferenceTemplates(
            TemplateEventListener listener,
            TemplateStore store) {
        this.listener = listener;
        this.store = store;
    }

    protected void loadReferenceTemplates() throws StoreException {
        for (String identifier : store.getReferenceIdentifiers()) {
            try {
                loadReferenceTemplate(identifier);
            } catch (Exception ex) {
                LOG.error("Can't load template: {}", identifier, ex);
            }
        }
    }

    private void loadReferenceTemplate(String identifier)
            throws BaseException {
        List<Statement> definitionStatements =
                store.getReferenceDefinition(identifier);
        ReferenceDefinition definition =
                ReferenceDefinitionAdapter.create(definitionStatements);
        if (definition == null) {
            throw new BaseException("Missing reference template definition");
        }
        List<Statement> configurationStatements =
                store.getReferenceConfiguration(identifier);
        ReferenceContainer container = new ReferenceContainer();
        container.identifier = identifier;
        container.resource = definition.resource;
        container.definition = definition;
        container.definitionStatements =
                Statements.wrap(definitionStatements);
        container.configurationStatements =
                Statements.wrap(configurationStatements);
        listener.onReferenceTemplateLoaded(container);
    }

}
