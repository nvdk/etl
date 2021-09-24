package com.linkedpipes.etl.storage.template.store;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Memory store for statements used fot testing.
 */
public class StatementsTemplateStore implements TemplateStore {

    private Map<String, List<Statement>> store = new HashMap<>();

    private Integer counter = 0;

    @Override
    public String getName() {
        return "statements-template-store";
    }

    @Override
    public List<String> getReferencesIri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String reserveIri(String domain) {
        return domain + COMPONENT_IRI_SUFFIX + (++counter);
    }

    @Override
    public void setPlugin(
            String iri,
            Collection<Statement> definition,
            Collection<Statement> configuration,
            Collection<Statement> configurationDescription) {
        store.put("definition:" + iri, new ArrayList<>(definition));
        store.put("configuration:" + iri, new ArrayList<>(definition));
        store.put("configurationDesc:" + iri, new ArrayList<>(definition));
    }

    @Override
    public List<Statement> getPluginDefinition(String iri) {
        return store.get("definition:" + iri);
    }

    @Override
    public List<Statement> getReferenceDefinition(String iri) {
        return store.get("definition:" + iri);
    }

    @Override
    public void setReferenceDefinition(
            String iri, Collection<Statement> statements) {
        store.put("definition:" + iri, new ArrayList<>(statements));
    }

    @Override
    public List<Statement> getPluginConfiguration(String iri) {
        return store.get("configuration:" + iri);
    }

    @Override
    public List<Statement> getReferenceConfiguration(String iri) {
        return store.get("configuration:" + iri);
    }

    @Override
    public void setReferenceConfiguration(
            String iri, Collection<Statement> statements) {
        store.put("configuration:" + iri, new ArrayList<>(statements));
    }

    @Override
    public List<Statement> getPluginConfigurationDescription(String iri) {
        return store.get("configurationDesc:" + iri);
    }

    @Override
    public byte[] getPluginFile(String iri, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPluginFile(String iri, String path, byte[] content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeReference(String iri) {
        throw new UnsupportedOperationException();
    }

    public Set<Statement> getStatements() {
        Model model = new LinkedHashModel();
        for (List<Statement> statements : store.values()) {
            model.addAll(statements);
        }
        return model;
    }

}
