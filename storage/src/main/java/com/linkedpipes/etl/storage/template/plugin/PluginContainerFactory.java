package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.plugin.configuration.ConfigurationDescriptionDefinition;
import com.linkedpipes.etl.plugin.configuration.ConfigurationDescriptionDefinitionAdapter;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.storage.utils.LocalizeStatements;
import com.linkedpipes.etl.storage.utils.Statements;
import com.linkedpipes.etl.storage.template.TemplateException;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginContainerFactory {

    protected static final Logger LOG =
            LoggerFactory.getLogger(PluginContainerFactory.class);

    protected final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    public PluginContainer create(PluginJarFile plugin)
            throws TemplateException {
        ConfigurationDescriptionDefinition description;
        try {
            description = ConfigurationDescriptionDefinitionAdapter.create(
                    plugin.getConfigurationDescription());
        } catch (InvalidConfiguration ex) {
            throw new TemplateException(
                    "Invalid configuration description.", ex);
        }

        Resource configuration = findConfiguration(
                plugin.getConfiguration(),
                description.forConfigurationType);

        Resource targetConfiguration = getConfigurationResource(plugin);
        Resource targetDescription = getDescriptionResource(plugin);
        PluginDefinition definition = createDefinition(
                plugin, targetConfiguration, targetDescription);
        description.resource = targetDescription;

        PluginContainer result = new PluginContainer();
        // We allow only one plugin per jar-file.
        result.identifier = "jar-" + plugin.getFile().getName();
        result.resource = valueFactory.createIRI(plugin.getPluginIri());
        result.definitionStatements =
                PluginDefinitionAdapter.asStatements(definition)
                        .withGraph(definition.resource);
        result.configurationStatements = updateStatements(
                plugin.getConfiguration(),
                configuration,
                targetConfiguration);
        result.configurationDescriptionStatements =
                Statements.wrap(
                        ConfigurationDescriptionDefinitionAdapter.asStatements(
                                description));
        result.files = loadFiles(plugin);
        result.definition = definition;
        return result;
    }


    protected Resource findConfiguration(
            Collection<Statement> configuration,
            Resource configurationType) throws TemplateException {

        Collection<Resource> configurationCandidates =
                Statements.wrap(configuration)
                        .select(null, RDF.TYPE, configurationType)
                        .subjects();
        if (configurationCandidates.size() != 1) {
            LOG.info("Expected one configuration class got {} for type {}",
                    configurationCandidates.size(),
                    configurationType);
            throw new TemplateException("Invalid configuration.");
        }
        return configurationCandidates.iterator().next();
    }

    protected Resource getConfigurationResource(PluginJarFile plugin) {
        return valueFactory.createIRI(
                plugin.getPluginIri() + "/configuration");
    }

    protected Statements updateStatements(
            Collection<Statement> statements,
            Resource source, Resource target) {
        Statements result = Statements.wrap(statements).withGraph(target);
        return Statements.wrap(LocalizeStatements.withPrefix(
                result, source, target.stringValue()));
    }

    protected Resource getDescriptionResource(PluginJarFile plugin) {
        return valueFactory.createIRI(
                plugin.getPluginIri() + "/configuration-description");
    }

    protected PluginDefinition createDefinition(
            PluginJarFile plugin, Resource configuration,
            Resource description) throws TemplateException {
        PluginDefinition result = PluginDefinitionAdapter.create(
                plugin.getDefinition());
        if (result == null) {
            LOG.info("Invalid definition for '{}'", plugin.getPluginIri());
            throw new TemplateException("Invalid plugin definition.");
        }
        result.configurationGraph = configuration;
        result.configurationDescriptionGraph = description;
        result.dialogs.addAll(plugin.getDialogEntries().keySet());
        return result;
    }

    protected Map<String, byte[]> loadFiles(PluginJarFile plugin)
            throws TemplateException {
        Map<String, byte[]> result = new HashMap<>();
        JarFile jar = plugin.getJarFile();
        for (var entry : plugin.getDialogEntries().entrySet()) {
            try {
                result.put(
                        "dialog/" + entry.getKey(),
                        readJarEntry(jar, entry.getValue()));
            } catch (IOException ex) {
                LOG.info("Can't read '{}' from '{}'.",
                        entry.getKey(), plugin.getPluginIri());
                throw new TemplateException("Can't read entry.", ex);
            }
        }
        return result;
    }

    protected byte[] readJarEntry(JarFile jar, JarEntry entry)
            throws IOException {
        try (InputStream stream = jar.getInputStream(entry)) {
            return stream.readAllBytes();
        }
    }

}
