package com.linkedpipes.etl.storage.template.store;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Memory store for statements used fot testing.
 */
public class StatementsStore implements TemplateStore {

    Model model = new LinkedHashModel();

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getReferenceIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String reserveIdentifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Statement> getPluginInterface(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlugin(
            String id,
            Collection<Statement> definition,
            Collection<Statement> configuration,
            Collection<Statement> configurationDescription) {
        model.addAll(definition);
        model.addAll(configuration);
        model.addAll(configurationDescription);
    }

    @Override
    public List<Statement> getReferenceInterface(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReferenceInterface(
            String id, Collection<Statement> statements) {
        model.addAll(statements);
    }

    @Override
    public List<Statement> getPluginDefinition(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Statement> getReferenceDefinition(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReferenceDefinition(
            String id, Collection<Statement> statements) {
        model.addAll(statements);
    }

    @Override
    public List<Statement> getPluginConfiguration(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Statement> getReferenceConfiguration(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReferenceConfiguration(
            String id, Collection<Statement> statements) {
        model.addAll(statements);
    }

    @Override
    public List<Statement> getPluginConfigurationDescription(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getPluginFile(String id, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPluginFile(String id, String path, byte[] content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeReference(String id) {
        throw new UnsupportedOperationException();
    }

    public Set<Statement> getStatements() {
        return model;
    }

}
