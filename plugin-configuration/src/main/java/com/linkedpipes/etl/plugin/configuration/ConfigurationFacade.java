package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationFacade {

    public List<Statement> createNewFromJarFile(
            List<Statement> configurationRdf, List<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws InvalidConfiguration {
        ConfigurationDescriptionDefinition description =
                loadDescription(descriptionRdf);
        return (new CreateNewConfiguration()).createNewFromJarFile(
                configurationRdf, description, baseIri, graph);
    }

    protected ConfigurationDescriptionDefinition loadDescription(
            List<Statement> statements)
            throws InvalidConfiguration {
        return ConfigurationDescriptionDefinitionAdapter.create(statements);
    }

    public List<Statement> createNewFromTemplate(
            List<Statement> configurationRdf, List<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws InvalidConfiguration {
        ConfigurationDescriptionDefinition description =
                loadDescription(descriptionRdf);
        return (new CreateNewConfiguration()).createNewFromTemplate(
                configurationRdf, description, baseIri, graph);
    }

    /**
     * The configurationsRdf must start with the template.
     */
    public List<Statement> merge(
            List<List<Statement>> configurationsRdf,
            List<Statement> descriptionRdf,
            String baseIri, IRI graph) throws InvalidConfiguration {
        ConfigurationDescriptionDefinition description =
                loadDescription(descriptionRdf);
        MergeConfiguration mergeConfiguration = new MergeConfiguration();
        List<Statement> result = new ArrayList<>(configurationsRdf.get(0));
        for (int index = 1; index < configurationsRdf.size(); ++index) {
            result = mergeConfiguration.merge(
                    result,
                    configurationsRdf.get(index),
                    description,
                    baseIri,
                    graph
            );
        }
        return result;
    }

    /**
     * Return private configuration properties.
     */
    public List<Statement> selectPrivateStatements(
            List<Statement> configurationRdf,
            List<Statement> descriptionRdf) throws InvalidConfiguration {
        ConfigurationDescriptionDefinition description =
                loadDescription(descriptionRdf);
        return (new SelectPrivateStatements())
                .selectPrivate(description, configurationRdf);
    }

}
