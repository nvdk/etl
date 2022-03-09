package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescriptionAdapter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigurationFacade {

    protected ConfigurationFacade() {
    }

    /**
     * Given plugin configuration returns configuration that should be used
     * by new instances of given component. Basically just create a copy.
     */
    public static List<Statement> createNewFromJarFile(
            List<Statement> configurationRdf,
            ConfigurationDescription description,
            String baseIri, Resource graph) {
        return (new CreateNewConfiguration()).createNewFromJarFile(
                configurationRdf, description, baseIri, graph);
    }

    protected static ConfigurationDescription loadDescription(
            List<Statement> statements) {
        return ConfigurationDescriptionAdapter
                .asConfigurationDescription(statements);
    }

    /**
     * Given template configuration returns configuration that should be used
     * by new instance of given component. Employ inheritance as a default
     * configuration value.
     */
    public static List<Statement> createNewFromTemplate(
            List<Statement> configurationRdf,
            ConfigurationDescription description,
            String baseIri, Resource graph) {
        return (new CreateNewConfiguration()).createNewFromTemplate(
                configurationRdf, description, baseIri, graph);
    }

    /**
     * THe list of configurations must start with the root, i.e. the plugin
     * template. The component instance should be the last one.
     * <p>
     * This function merge the content of the configurations, so it can be
     * used for execution.
     */
    public static List<Statement> merge(
            List<List<Statement>> configurationsRdf,
            List<Statement> descriptionRdf,
            String baseIri, Resource graph) throws InvalidConfiguration {
        ConfigurationDescription description = loadDescription(descriptionRdf);
        return merge(configurationsRdf, description, baseIri, graph);
    }

    public static List<Statement> merge(
            List<List<Statement>> configurationsRdf,
            ConfigurationDescription description,
            String baseIri, Resource graph) throws InvalidConfiguration {
        MergeConfiguration mergeConfiguration = new MergeConfiguration();
        List<Statement> result = new ArrayList<>(configurationsRdf.get(0));
        for (int index = 1; index < configurationsRdf.size(); ++index) {
            result = mergeConfiguration.merge(
                    result, configurationsRdf.get(index), description,
                    baseIri, graph);
        }
        return result;
    }

    /**
     * Remove all statements that represent private part of the configuration.
     */
    public static List<Statement> removePrivateStatements(
            Collection<Statement> configuration,
            ConfigurationDescription description) {
        List<Statement> result = new ArrayList<>(configuration);
        result.removeAll(selectPrivateStatements(configuration, description));
        return result;
    }

    /**
     * Return statements that represent the private part of a configuration
     * as described by given description.
     */
    public static List<Statement> selectPrivateStatements(
            Collection<Statement> configuration,
            ConfigurationDescription description) {
        return (new SelectPrivateStatements()).selectPrivate(
                description, configuration);
    }

}
