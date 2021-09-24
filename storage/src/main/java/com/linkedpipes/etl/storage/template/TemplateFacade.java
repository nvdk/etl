package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.template.list.PluginList;
import com.linkedpipes.etl.storage.template.list.TemplateList;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.unpacker.TemplateSource;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class TemplateFacade implements TemplateSource {

    private final TemplateService templateService;

    private final PluginList pluginList;

    private final TemplateList templateList;

    @Autowired
    public TemplateFacade(Configuration configuration) {
        this.pluginList = new PluginList();
        this.templateList = new TemplateList();
        //
        TemplateEventNotifier notifier = new TemplateEventNotifier();
        notifier.addListener(this.pluginList);
        notifier.addListener(this.templateList);
        this.templateService = new TemplateService(configuration, notifier);
    }

    @PostConstruct
    public void initialize() throws BaseException {
        this.templateService.initialize();
    }

    public Template getTemplate(String iri) {
        return templateList.getTemplate(iri);
    }

    public Collection<Template> getTemplates() {
        return templateList.getTemplates();
    }

    public Template getParent(Template template) {
        return templateList.getParent(template);
    }

    public Template getRootTemplate(Template template) {
        return templateList.getRootTemplate(template);
    }

    public List<Template> getAncestors(Template template) {
        return templateList.getAncestors(template);
    }

    public List<Template> getAncestorsWithoutJarTemplate(Template template) {
        return templateList.getAncestorsWithoutJarTemplate(template);
    }

    public Collection<Template> getSuccessors(Template template) {
        return templateList.getSuccessors(template);
    }

    public Collection<Statement> getInterface(Template template)
            throws BaseException {
        return templateService.getDefinition(template);
    }

    public Collection<Statement> getInterfaces() throws BaseException {
        List<Statement> output = new ArrayList<>();
        Collection<Template> templates = templateList.getTemplates();
        for (Template template : templates) {
            output.addAll(getInterface(template));
        }
        return output;
    }

    /**
     * Return template config for execution or as merged parent configuration.
     * Configuration of all ancestors are applied.
     */
    public Collection<Statement> getEffectiveConfiguration(Template template)
            throws BaseException, InvalidConfiguration {
        if (template.isPluginTemplate()
                && ((PluginTemplate)template).isSupportControl()) {
            // For template without inheritance control, the current
            // configuration is the effective one.
            return getConfiguration(template);
        }
        List<Statement> description =
                (new Statements(getConfigurationDescription(template))).asList();
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<List<Statement>> configurations = new ArrayList<>();
        for (Template item : getAncestors(template)) {
            configurations.add(new ArrayList<>(getConfiguration(item)));
        }

        return (new ConfigurationFacade()).merge(
                configurations,
                description,
                template.getIri() + "/effective/",
                valueFactory.createIRI(template.getIri()));
    }

    /**
     * Return configuration of given template for a dialog.
     */
    public Collection<Statement> getConfiguration(Template template)
            throws BaseException {
        return templateService.getConfiguration(template);
    }

    /**
     * Return configuration for instances of given template.
     */
    public Collection<Statement> getInstanceConfiguration(Template template)
            throws BaseException, InvalidConfiguration {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graph = valueFactory.createIRI(template.getIri() + "/new");
        List<Statement> configuration =
                templateService.getConfiguration(template);
        if (template.isPluginTemplate()) {
            return (new ConfigurationFacade()).createNewFromJarFile(
                    configuration,
                    getConfigurationDescription(template),
                    graph.stringValue(),
                    graph);
        } else if (template.isReferenceTemplate()) {
            return (new ConfigurationFacade()).createNewFromTemplate(
                    configuration,
                    getConfigurationDescription(template),
                    graph.stringValue(),
                    graph);
        } else {
            throw new BaseException(
                    "Unknown template type: {}", template.getIri());
        }
    }

    public List<Statement> getConfigurationDescription(Template template)
            throws BaseException {
        Template root = getRootTemplate(template);
        if (!root.isPluginTemplate()) {
            throw new BaseException(
                    "Root template '{}' is not a plugin for '{}'",
                    root.getIri(), template.getIri());
        }
        PluginTemplate pluginTemplate = (PluginTemplate) root;
        return templateService.getConfigurationDescription(pluginTemplate);
    }

    public byte[] getDialogResource(
            Template template, String dialog, String path)
            throws StoreException {
        return templateService.getPluginFile(
                template, "dialog/" + dialog + "/" + path);
    }

    public byte[] getStaticResource(Template template, String path)
            throws StoreException {
        return templateService.getPluginFile(template, "static/" + path);
    }

    public Template createReferenceTemplate(
            Collection<Statement> definition,
            Collection<Statement> configuration)
            throws BaseException {
        return templateService.createReferenceTemplate(definition, configuration);
    }

    public void updateReferenceInterface(
            ReferenceTemplate template, Collection<Statement> statements)
            throws BaseException {
        templateService.updateReferenceTemplate(template, statements);
    }

    public void updateConfiguration(
            Template template, Collection<Statement> statements)
            throws BaseException {
        templateService.updateReferenceConfiguration(template, statements);
    }

    public void removeReference(ReferenceTemplate template)
            throws BaseException {
        templateService.removeReference(template);
    }

    public Collection<Statement> getDefinition(Template template)
            throws BaseException {
        return templateService.getDefinition(template);
    }

    public PluginJarFile getPluginJar(String iri) {
        return pluginList.getPluginJarFile(iri);
    }

    // TemplateSource

    @Override
    public Collection<Statement> getDefinition(String iri)
            throws BaseException {
        Template template = getTemplate(iri);
        return getDefinition(template);
    }

    @Override
    public Collection<Statement> getConfiguration(String iri)
            throws BaseException {
        return getConfiguration(getTemplate(iri));
    }

    @Override
    public Collection<Statement> getConfigurationDescription(String iri)
            throws BaseException {
        return getConfigurationDescription(getTemplate(iri));
    }

}
