package com.linkedpipes.etl.executor.unpacker;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.unpacker.model.GraphCollection;
import com.linkedpipes.etl.executor.unpacker.model.template.Template;
import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ConfigurationMerger {

    private final GraphCollection graphs;

    private final TemplateSource templateSource;

    public ConfigurationMerger(GraphCollection graphs,
                               TemplateSource templateSource) {
        this.graphs = graphs;
        this.templateSource = templateSource;
    }

    public void loadTemplateConfigAndDescription(Template template)
            throws ExecutorException {
        loadConfigTemplate(template.getIri(),
                template.getConfigGraph());
        loadConfigDescription(template.getIri(),
                template.getConfigDescriptionGraph());
    }

    private void loadConfigTemplate(String iri, String graph)
            throws ExecutorException {
        if (graphs.containsKey(graph)) {
            return;
        }
        Collection<Statement> statements = templateSource.getConfiguration(iri);
        graphs.put(graph, statements);
    }

    private void loadConfigDescription(String iri, String graph)
            throws ExecutorException {
        if (graphs.containsKey(graph)) {
            return;
        }
        Collection<Statement> statements =
                templateSource.getConfigurationDescription(iri);
        graphs.put(graph, statements);
    }

    public void copyConfigurationGraphs(String source, String target) {
        graphs.put(target, changeGraph(graphs.get(source), target));
    }

    private Collection<Statement> changeGraph(
            Collection<Statement> statements, String graph) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graphIri = valueFactory.createIRI(graph);
        return statements.stream().map(s -> valueFactory.createStatement(
                s.getSubject(), s.getPredicate(), s.getObject(), graphIri))
                .collect(Collectors.toList());
    }

    public void merge(
            Template template, List<String> configurations, String targetGraph)
            throws ExecutorException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<Statement> description = new ArrayList<>(
                graphs.get(template.getConfigDescriptionGraph()));
        List<Statement> result;
        List<List<Statement>> configurationsRdf = configurations.stream()
                .map(iri -> new ArrayList<>(graphs.get(iri)))
                .collect(Collectors.toList());
        Collections.reverse(configurationsRdf);
        try {
            result = ConfigurationFacade.merge(
                    configurationsRdf,
                    description,
                    targetGraph,
                    valueFactory.createIRI(targetGraph)
            );
        } catch (InvalidConfiguration ex) {
            throw new ExecutorException(
                    "Can't merge configuration for: {}",
                    template.getIri(), ex);
        }
        graphs.put(targetGraph, result);
    }

}
