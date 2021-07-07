package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.mapping.MappingFacade;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.unpacker.TemplateSource;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class TemplateFacade implements TemplateSource {

    private static final Logger LOG
            = LoggerFactory.getLogger(TemplateFacade.class);

    private final TemplateManager manager;

    private final MappingFacade mapping;

    private final TemplateStore store;

    private final ConfigurationFacade configurationFacade;

    @Autowired
    public TemplateFacade(
            TemplateManager manager,
            MappingFacade mapping) {
        this.manager = manager;
        this.mapping = mapping;
        this.configurationFacade = new ConfigurationFacade();
        this.store = manager.getStore();
    }

    @PostConstruct
    public void initialize() {
        cleanMapping();
    }

    /**
     * Remove mapping for components that are no longer in the system.
     */
    private void cleanMapping() {
        List<String> iriToRemove = mapping.getLocalMapping().stream()
                .filter(iri -> !manager.getTemplates().containsKey(iri))
                .collect(Collectors.toList());
        for (String iri : iriToRemove) {
            LOG.debug(
                    "Removing mapping for '{}' as the component was deleted.",
                    iri);
            mapping.remove(iri);
        }
        mapping.save();
    }

    public Template getTemplate(String iri) {
        return manager.getTemplates().get(iri);
    }

    public Collection<Template> getTemplates() {
        return manager.getTemplates().values();
    }

    public Template getParent(Template template) {
        if (template instanceof ReferenceTemplate) {
            ReferenceTemplate ref = (ReferenceTemplate) template;
            return getTemplate(ref.getTemplate());
        }
        return null;
    }

    public Template getRootTemplate(Template template) {
        if (template instanceof PluginTemplate) {
            return template;
        } else if (template instanceof ReferenceTemplate) {
            ReferenceTemplate referenceTemplate = (ReferenceTemplate) template;
            return referenceTemplate.getCoreTemplate();
        } else {
            throw new RuntimeException("Unknown component type.");
        }
    }

    /**
     * The path from root (template) to the given template.
     */
    public List<Template> getAncestors(Template template) {
        LinkedList<Template> templates = collectAncestors(template);
        Collections.reverse(templates);
        return templates;
    }

    private LinkedList<Template> collectAncestors(Template template) {
        LinkedList<Template> output = new LinkedList<>();
        while (true) {
            output.add(template);
            if (template.isPlugin()) {
                break;
            } else if (template.isReference()) {
                ReferenceTemplate reference = (ReferenceTemplate) template;
                template = getTemplate(reference.getTemplate());
                if (template == null) {
                    LOG.warn("Missing template for: {}", reference.getIri());
                    break;
                }
            } else {
                throw new RuntimeException("Unknown template type: "
                        + template.getId());
            }
        }
        return output;
    }

    public List<Template> getAncestorsWithoutJarTemplate(Template template) {
        LinkedList<Template> templates = collectAncestors(template);
        templates.remove(templates.removeLast());
        Collections.reverse(templates);
        return templates;
    }

    public Collection<Template> getSuccessors(Template template) {
        Map<Template, List<Template>> children = buildChildrenIndex();
        Set<Template> output = new HashSet<>();
        Set<Template> toTest = new HashSet<>();
        toTest.addAll(children.getOrDefault(
                template, Collections.EMPTY_LIST));
        while (!toTest.isEmpty()) {
            Template item = toTest.iterator().next();
            toTest.remove(item);
            if (output.contains(item)) {
                continue;
            }
            List<Template> itemChildren = children.getOrDefault(
                    item, Collections.EMPTY_LIST);
            output.add(item);
            output.addAll(itemChildren);
            toTest.addAll(itemChildren);
        }
        return output;
    }

    private Map<Template, List<Template>> buildChildrenIndex() {
        Map<Template, List<Template>> children = new HashMap<>();
        for (Template item : getTemplates()) {
            if (!item.isReference()) {
                continue;
            }
            ReferenceTemplate reference = (ReferenceTemplate) item;
            Template parent = getTemplate(reference.getTemplate());
            List<Template> brothers = children.computeIfAbsent(
                    parent, key -> new LinkedList<>());
            // Create if does not exists.
            brothers.add(reference);
        }
        return children;
    }

    public Collection<Statement> getInterface(Template template)
            throws BaseException {
        if (template.isPlugin()) {
            return store.getPluginInterface(template.getId());
        } else if (template.isReference()) {
            return store.getReferenceInterface(template.getId());
        } else {
            throw new BaseException("Unknown template: {}", template.getId());
        }
    }

    public Collection<Statement> getInterfaces() throws BaseException {
        List<Statement> output = new ArrayList<>();
        Collection<Template> templates = manager.getTemplates().values();
        for (Template template : templates) {
            output.addAll(getInterface(template));
        }
        return output;
    }

    /**
     * Return template config for execution or as merged parent configuration.
     * Configuration of all ancestors are applied.
     */
    public Collection<Statement> getConfigEffective(Template template)
            throws BaseException, InvalidConfiguration {
        if (template.isPlugin()
                && ((PluginTemplate)template).isSupportControl()) {
            // For template without inheritance control, the current
            // configuration is the effective one.
            return getConfig(template);
        }
        List<Statement> description =
                (new Statements(getConfigDescription(template))).asList();
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<List<Statement>> configurations = new ArrayList<>();
        for (Template item : getAncestors(template)) {
            configurations.add(new ArrayList<>(getConfig(item)));
        }
        return configurationFacade.merge(
                configurations,
                description,
                template.getIri() + "/effective/",
                valueFactory.createIRI(template.getIri()));
    }

    /**
     * Return configuration of given template for a dialog.
     */
    public Collection<Statement> getConfig(Template template)
            throws BaseException {
        if (template.isPlugin()) {
            return store.getPluginConfiguration(template.getId());
        } else if (template.isReference()) {
            return store.getReferenceConfiguration(template.getId());
        } else {
            throw new BaseException("Unknown template type: {}",
                    template.getId());
        }
    }

    /**
     * Return configuration for instances of given template.
     */
    public Collection<Statement> getConfigInstance(Template template)
            throws BaseException, InvalidConfiguration {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graph = valueFactory.createIRI(template.getIri() + "/new");
        if (template.isPlugin()) {
            return configurationFacade.createNewFromJarFile(
                    store.getPluginConfiguration(template.getId()),
                    getConfigDescription(template),
                    graph.stringValue(),
                    graph);
        } else if (template.isReference()) {
            return configurationFacade.createNewFromTemplate(
                    store.getReferenceConfiguration(template.getId()),
                    getConfigDescription(template),
                    graph.stringValue(),
                    graph);
        } else {
            throw new BaseException("Unknown template type: {}",
                    template.getId());
        }
    }

    public List<Statement> getConfigDescription(Template template)
            throws BaseException {
        Template root = getRootTemplate(template);
        if (!root.isPlugin()) {
            throw new BaseException(
                    "Root template '{}' is not a plugin for '{}'",
                    root.getId(), template.getId());
        }
        return store.getPluginConfigurationDescription(root.getId());
    }

    public byte[] getDialogResource(
            Template template, String dialog, String path)
            throws StoreException {
        return store.getPluginFile(
                template.getId(), "dialog/" + dialog + "/" + path);
    }

    public byte[] getStaticResource(Template template, String path)
            throws StoreException {
        return store.getPluginFile(
                template.getId(), "static/" + path);
    }

    public Template createReferenceTemplate(
            Collection<Statement> definition,
            Collection<Statement> configuration)
            throws BaseException {
        return manager.createReferenceTemplate(definition, configuration);
    }

    public void updateReferenceInterface(
            ReferenceTemplate template, Collection<Statement> diff)
            throws BaseException {
        manager.updateReferenceTemplateInterface(template, diff);
    }

    public void updateConfig(
            Template template, Collection<Statement> statements)
            throws BaseException {
        manager.updateReferenceConfiguration(template, statements);
    }

    public void removeReference(ReferenceTemplate template)
            throws BaseException {
        manager.removeReference(template);
        mapping.remove(template.getIri());
        mapping.save();
    }

    public Collection<Statement> getDefinition(Template template)
            throws BaseException {
        if (template.isPlugin()) {
            return store.getPluginDefinition(template.getId());
        } else if (template.isReference()) {
            return store.getReferenceDefinition(template.getId());
        } else {
            throw new BaseException("Unknown template: {}", template.getId());
        }
    }

    public PluginJarFile getPluginJar(String iri) {
        return manager.getPluginJar(iri);
    }

    @Override
    public Collection<Statement> getDefinition(String iri)
            throws BaseException {
        Template template = getTemplate(iri);
        return getDefinition(template);
    }

    @Override
    public Collection<Statement> getConfiguration(String iri)
            throws BaseException {
        return getConfig(getTemplate(iri));
    }

    @Override
    public Collection<Statement> getConfigurationDescription(String iri)
            throws BaseException {
        return getConfigDescription(getTemplate(iri));
    }

}
