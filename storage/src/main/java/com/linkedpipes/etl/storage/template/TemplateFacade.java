package com.linkedpipes.etl.storage.template;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.StorageVersion;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplateApi;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplateApi;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class TemplateFacade implements TemplateApi {

    private final PluginTemplateApi pluginApi;

    private final ReferenceTemplateApi referenceApi;

    public TemplateFacade(
            PluginTemplateApi pluginApi,
            ReferenceTemplateApi referenceApi) {
        this.pluginApi = pluginApi;
        this.referenceApi = referenceApi;
    }

    @Override
    public boolean isPluginTemplate(String iri) throws StorageException {
        return pluginApi.listPluginTemplates().contains(iri);
    }

    @Override
    public boolean isReferenceTemplate(String iri) throws StorageException {
        return referenceApi.listReferenceTemplates().contains(iri);
    }

    @Override
    public Optional<String> getParent(String iri) throws StorageException {
        if (isReferenceTemplate(iri)) {
            Optional<ReferenceTemplate> templateOptional =
                    referenceApi.loadReferenceTemplate(iri);
            if (templateOptional.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(templateOptional.get().template().stringValue());
        }
        if (isPluginTemplate(iri)) {
            return Optional.empty();
        } else {
            throw new StorageException("Missing template {}", iri);
        }
    }

    @Override
    public List<String> getAncestors(String iri) throws StorageException {
        List<String> result = new ArrayList<>();
        Optional<String> next = getParent(iri);
        while (next.isPresent()) {
            result.add(next.get());
            next = getParent(next.get());
        }
        // If there are no templates iri may refer to plugin, so we add
        // just the IRI.
        if (result.isEmpty()) {
            result.add(iri);
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<String> getSuccessors(String iri) throws StorageException {
        Stack<ReferenceTemplate> toVisit = new Stack<>();
        List<ReferenceTemplate> templates = new ArrayList<>();
        for (String templateIri : listReferenceTemplates()) {
            Optional<ReferenceTemplate> templateWrap =
                    loadReferenceTemplate(templateIri);
            if (templateWrap.isEmpty()) {
                continue;
            }
            ReferenceTemplate template = templateWrap.get();
            templates.add(template);
            if (template.template().stringValue().equals(iri)) {
                toVisit.add(template);
            }
        }
        List<String> result = new ArrayList<>();
        while (!toVisit.isEmpty()) {
            ReferenceTemplate next = toVisit.pop();
            Resource nextResource = next.resource();
            if (result.contains(nextResource.stringValue())) {
                continue;
            }
            result.add(nextResource.stringValue());
            templates.stream()
                    .filter(item -> item.template().equals(nextResource))
                    .forEach(toVisit::add);
        }
        return result;
    }

    @Override
    public Optional<Statements> getEffectiveConfiguration(String iri)
            throws StorageException {
        List<String> ancestors = getAncestors(iri);
        Optional<PluginTemplate> pluginOptional =
                loadPluginTemplate(ancestors.get(0));
        if (pluginOptional.isEmpty()) {
            return Optional.empty();
        }
        if (Objects.equal(iri, ancestors.get(0))) {
            // The given IRI is a plugin template, we just return the
            // configuration.
            return Optional.of(pluginOptional.get().configuration());
        }
        Optional<ReferenceTemplate> referenceTemplateOptional
                = loadReferenceTemplate(iri);
        if (referenceTemplateOptional.isEmpty()) {
            return Optional.empty();
        }
        List<List<Statement>> configurations = new ArrayList<>();
        configurations.add(pluginOptional.get().configuration().asList());
        Iterator<String> iterator = ancestors.iterator();
        iterator.next(); // Skip plugin template.
        while (iterator.hasNext()) {
            Optional<ReferenceTemplate> templateOptional =
                    loadReferenceTemplate(iterator.next());
            if (templateOptional.isEmpty()) {
                continue;
            }
            configurations.add(templateOptional.get().configuration().asList());
        }
        ReferenceTemplate template = referenceTemplateOptional.get();
        configurations.add(template.configuration().asList());
        List<Statement> result;
        try {
            result = ConfigurationFacade.merge(
                    configurations,
                    pluginOptional.get().configurationDescription(),
                    template.configurationGraph().stringValue(),
                    template.configurationGraph());
        } catch (InvalidConfiguration ex) {
            throw new StorageException("Can't merge configurations.", ex);
        }
        return Optional.of(Statements.wrap(result));
    }

    @Override
    public Optional<Statements> getNewConfiguration(String iri)
            throws StorageException {
        if (isPluginTemplate(iri)) {
            Optional<PluginTemplate> pluginOptional =
                    loadPluginTemplate(iri);
            if (pluginOptional.isEmpty()) {
                return Optional.empty();
            }
            PluginTemplate plugin = pluginOptional.get();
            List<Statement> result = ConfigurationFacade.createNewFromJarFile(
                    plugin.configuration().asList(),
                    plugin.configurationDescription(),
                    plugin.configurationGraph().stringValue(),
                    plugin.configurationGraph());
            return Optional.of(Statements.wrap(result));
        } else if (isReferenceTemplate(iri)) {
            Optional<ReferenceTemplate> referenceOptional =
                    loadReferenceTemplate(iri);
            if (referenceOptional.isEmpty()) {
                return Optional.empty();
            }
            ReferenceTemplate reference = referenceOptional.get();
            Optional<PluginTemplate> pluginOptional =
                    loadPluginTemplate(reference.pluginTemplate().stringValue());
            if (pluginOptional.isEmpty()) {
                return Optional.empty();
            }
            PluginTemplate plugin = pluginOptional.get();
            List<Statement> result = ConfigurationFacade.createNewFromTemplate(
                    reference.configuration().asList(),
                    plugin.configurationDescription(),
                    reference.configurationGraph().stringValue(),
                    reference.configurationGraph());
            return Optional.of(Statements.wrap(result));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<String> listPluginTemplates()
            throws StorageException {
        return pluginApi.listPluginTemplates();
    }

    @Override
    public Optional<PluginTemplate> loadPluginTemplate(String iri)
            throws StorageException {
        return pluginApi.loadPluginTemplate(iri);
    }

    @Override
    public Collection<String> listReferenceTemplates()
            throws StorageException {
        return referenceApi.listReferenceTemplates();
    }

    @Override
    public Optional<ReferenceTemplate> loadReferenceTemplate(String iri)
            throws StorageException {
        return referenceApi.loadReferenceTemplate(iri);
    }

    @Override
    public ReferenceTemplate storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException {
        if (!StorageVersion.isCurrent(template.version())) {
            throw new StorageException(
                    "Invalid version: {}", template.version());
        }
        template = setPluginTemplate(template);
        return referenceApi.storeReferenceTemplate(template);
    }

    /**
     * Check plugin parent and set plugin template.
     */
    private ReferenceTemplate setPluginTemplate(ReferenceTemplate template)
            throws StorageException {
        // Check that the parent exists.
        if (template.template() == null) {
            throw new StorageException("Reference template must have parent.");
        }
        String parent = template.template().stringValue();
        Resource pluginTemplate;
        if (isReferenceTemplate(parent)) {
            Optional<ReferenceTemplate> parentTemplateOptional =
                    loadReferenceTemplate(parent);
            if (parentTemplateOptional.isEmpty()) {
                throw new StorageException(
                        "Missing reference template: {}", parent);
            }
            ReferenceTemplate parentTemplate = parentTemplateOptional.get();
            pluginTemplate = parentTemplate.pluginTemplate();
        } else if (isPluginTemplate(parent)) {
            pluginTemplate = template.template();
        } else {
            throw new StorageException("Unknown parent: {}", parent);
        }
        return new ReferenceTemplate(
                template.resource(), template.template(),
                template.prefLabel(), template.description(), template.note(),
                template.color(), template.tags(), template.knownAs(),
                pluginTemplate,
                template.version(), template.configuration(),
                template.configurationGraph());
    }

    @Override
    public void deleteReferenceTemplate(String iri)
            throws StorageException {
        referenceApi.deleteReferenceTemplate(iri);
    }

}
