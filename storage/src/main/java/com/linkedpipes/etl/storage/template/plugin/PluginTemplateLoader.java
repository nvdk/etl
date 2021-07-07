package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.utils.LocalizeStatements;
import com.linkedpipes.etl.storage.utils.Statements;
import com.linkedpipes.etl.storage.template.TemplateException;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class PluginTemplateLoader {

    private static final IRI CONFIGURATION_DESCRIPTION;

    private static final IRI HAS_CONFIG_TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        CONFIGURATION_DESCRIPTION = valueFactory.createIRI(
                LP_PIPELINE.CONFIGURATION_DESCRIPTION);
        HAS_CONFIG_TYPE = valueFactory.createIRI(
                LP_PIPELINE.HAS_CONFIG_TYPE);
    }

    protected static final Logger LOG =
            LoggerFactory.getLogger(PluginTemplateLoader.class);

    protected final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    protected String iri;

    protected String identifier;

    protected PluginDefinition definition;

    protected Collection<Statement> definitionStatements;

    protected Collection<Statement> configurationStatements;

    protected Collection<Statement> configurationDescriptionStatements;

    protected Map<String, byte[]> files;

    public void load(PluginJarFile plugin) throws TemplateException {
        Resource description = findDescription(
                plugin.getConfigurationDescription());

        Resource configuration = findConfiguration(
                plugin.getConfigurationDescription(),
                plugin.getConfiguration(),
                description);

        Resource targetConfiguration = getConfigurationResource(plugin);
        Resource targetDescription = getDescriptionResource(plugin);

        iri = plugin.getPluginIri();
        // We allow only one plugin per jar-file.
        identifier = "jar-" + plugin.getFile().getName();
        definition = createDefinition(
                plugin, targetConfiguration, targetDescription);
        definitionStatements = PluginDefinitionAdapter.asStatements(definition)
                .withGraph(definition.resource);
        configurationStatements = updateStatements(
                plugin.getConfiguration(),
                configuration, targetConfiguration);
        configurationDescriptionStatements = updateStatements(
                plugin.getConfigurationDescription(),
                description, targetDescription);
        files = loadFiles(plugin);
    }

    protected Resource findDescription(Collection<Statement> description)
            throws TemplateException {
        Statements statements = Statements.wrap(description);
        Collection<Resource> candidates = statements.select(
                null, RDF.TYPE, CONFIGURATION_DESCRIPTION).subjects();
        if (candidates.size() != 1) {
            LOG.info("Expected one configuration description got {}",
                    candidates.size());
            throw new TemplateException(
                    "Invalid configuration description.");
        }
        return candidates.iterator().next();
    }

    protected Resource findConfiguration(
            Collection<Statement> description,
            Collection<Statement> configuration,
            Resource descriptionResource) throws TemplateException {
        Collection<Value> configurationTypeCandidates =
                Statements.wrap(description)
                        .select(descriptionResource, HAS_CONFIG_TYPE, null)
                        .objects();
        if (configurationTypeCandidates.size() != 1) {
            LOG.info("Expected one configuration type got {}",
                    configurationTypeCandidates.size());
            throw new TemplateException(
                    "Invalid configuration description.");
        }
        Value configurationType = configurationTypeCandidates.iterator().next();
        if (!(configurationType instanceof Resource)) {
            LOG.info("Invalid configuration type {}",
                    configurationType);
            throw new TemplateException(
                    "Invalid configuration description.");
        }
        Collection<Resource> configurationCandidates =
                Statements.wrap(configuration)
                        .select(null, RDF.TYPE,
                                configurationTypeCandidates.iterator().next())
                        .subjects();
        if (configurationCandidates.size() != 1) {
            LOG.info("Expected one configuration class got {} for type {}",
                    configurationCandidates.size(),
                    configurationTypeCandidates.iterator().next());
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

    public String getIri() {
        return iri;
    }

    public String getIdentifier() {
        return identifier;
    }

    public PluginDefinition getDefinition() {
        return definition;
    }

    public Collection<Statement> getDefinitionStatements() {
        return definitionStatements;
    }

    public Collection<Statement> getConfigurationStatements() {
        return configurationStatements;
    }

    public Collection<Statement> getConfigurationDescriptionStatements() {
        return configurationDescriptionStatements;
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

}
