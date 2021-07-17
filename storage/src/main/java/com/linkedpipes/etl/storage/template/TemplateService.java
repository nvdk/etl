package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.migration.MigrateStore;
import com.linkedpipes.etl.storage.template.plugin.PluginContainer;
import com.linkedpipes.etl.storage.template.plugin.PluginContainerFactory;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinitionAdapter;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainerFactory;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreService;
import com.linkedpipes.plugin.loader.PluginJarFile;
import com.linkedpipes.plugin.loader.PluginLoader;
import com.linkedpipes.plugin.loader.PluginLoaderException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateService.class);

    private final Configuration configuration;

    private final Map<String, Template> templates = new HashMap<>();

    private final Map<String, PluginJarFile> plugins = new HashMap<>();

    private TemplateStore store;

    @Autowired
    public TemplateService(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void initialize() throws BaseException {
        TemplateStoreService storeService = new TemplateStoreService(
                configuration.getTemplatesDirectory());
        storeService.initialize();
        store = storeService.createStore();
        try {
            loadPluginTemplates();
            if (storeService.shouldMigrate()) {
                migrateStore(storeService);
            }
            loadReferenceTemplates();
        } catch (Exception ex) {
            LOG.error("Initialization failed.", ex);
            throw ex;
        }
    }

    private void loadPluginTemplates()
            throws TemplateException, StoreException {
        LOG.info("Loading plugins ...");
        PluginLoader loader = new PluginLoader();
        List<File> files = listPluginFiles(configuration.getJarDirectory());
        for (File file : files) {
            List<PluginJarFile> references;
            try {
                references = loader.loadPlugin(file);
            } catch (PluginLoaderException ex) {
                LOG.error("Can't load plugin from: {}", file, ex);
                continue;
            }
            for (PluginJarFile plugin : references) {
                loadPluginTemplate(plugin);
                plugins.put(plugin.getJarIri(), plugin);
            }
        }

        LOG.info("Loading plugins ... done (loaded {})", plugins.size());
    }

    private List<File> listPluginFiles(File directory) {
        return FileUtils.listFiles(directory, new String[]{"jar"}, true)
                .stream().filter(this::isPluginFile)
                .collect(Collectors.toList());
    }

    private boolean isPluginFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(".jar");
    }

    private void loadPluginTemplate(PluginJarFile pluginJarFile)
            throws StoreException, TemplateException {
        PluginContainer container = createPluginContainer(pluginJarFile);
        store.setPlugin(
                container.identifier,
                container.definitionStatements,
                container.configurationStatements,
                container.configurationDescriptionStatements);
        for (Map.Entry<String, byte[]> entry : container.files.entrySet()) {
            store.setPluginFile(
                    container.identifier,
                    entry.getKey(),
                    entry.getValue());
        }
        templates.put(
                container.resource.stringValue(),
                new PluginTemplate(container));
    }

    private PluginContainer createPluginContainer(PluginJarFile pluginJarFile)
            throws TemplateException {
        try {
            return (new PluginContainerFactory()).create(pluginJarFile);
        } catch (TemplateException ex) {
            LOG.info("Can't load plugin template: '{}' from '{}'",
                    pluginJarFile.getPluginIri(), pluginJarFile.getFile());
            throw ex;
        }
    }

    private void migrateStore(TemplateStoreService storeService)
            throws BaseException {
        TemplateStore source = storeService.createStoreFromInfoFile();
        MigrateStore migration =
                new MigrateStore(source, store, storeService.getStoreInfo());
        StoreInfo newStoreInfo = migration.migrate();
        storeService.setStoreInfo(newStoreInfo);
    }

    private void loadReferenceTemplates() throws StoreException {
        // We need to load all references first and then set their core
        // template once all is loaded as their can be dependencies.
        List<ReferenceTemplate> referenceTemplates = new ArrayList<>();
        for (String id : store.getReferenceIdentifiers()) {
            try {
                ReferenceTemplate template = loadReferenceTemplate(id);
                if (template.getIri() == null) {
                    LOG.error("Invalid template ignored: {}", id);
                    continue;
                }
                referenceTemplates.add(template);
                templates.put(template.getIri(), template);
            } catch (Exception ex) {
                LOG.error("Can't load template: {}", id, ex);
            }
        }
        setTemplateCoreReferences(referenceTemplates);
    }

    private ReferenceTemplate loadReferenceTemplate(String id)
            throws BaseException {
        Collection<Statement> definitionStatements =
                store.getReferenceDefinition(id);
        ReferenceDefinition definition =
                ReferenceDefinitionAdapter.create(definitionStatements);
        if (definition == null) {
            throw new BaseException("Missing reference template definition");
        }
        return new ReferenceTemplate(id, definition);
    }

    private void setTemplateCoreReferences(List<ReferenceTemplate> templates) {
        for (ReferenceTemplate template : templates) {
            template.setCorePlugin(findCorePlugin(template));
        }
    }

    private String findCorePlugin(ReferenceTemplate template) {
        while (true) {
            Template parent = templates.get(template.getTemplate());
            if (parent == null) {
                LOG.error("Missing parent for: {}", template.getIri());
                return null;
            }
            if (parent.getCorePlugin()) {
                return parent.iri;
            } else if (parent.isReference()) {
                template = (ReferenceTemplate) parent;
            } else {
                LOG.error("Invalid template type: {}", parent.getIri());
                return null;
            }
        }
    }

    public Template createReferenceTemplate(
            Collection<Statement> interfaceRdf,
            Collection<Statement> configurationRdf)
            throws BaseException {
        String id = store.reserveIdentifier();
        String iri = configuration.getDomainName()
                + "/resources/components/" + id;
        ReferenceContainerFactory factory = new ReferenceContainerFactory();
        try {
            ReferenceContainer container = factory.create(
                    id, iri, interfaceRdf, configurationRdf);
            ReferenceTemplate referenceTemplate =
                    new ReferenceTemplate(container);
            referenceTemplate.setCorePlugin(
                    findCorePlugin(referenceTemplate));
            templates.put(referenceTemplate.iri, referenceTemplate);
            return referenceTemplate;
        } catch (BaseException ex) {
            store.removeReference(id);
            throw ex;
        }
    }

    public void updateReferenceTemplate(
            ReferenceTemplate template, Collection<Statement> diff)
            throws BaseException {
        diff = RdfUtils.forceContext(diff, template.getIri());
        String id = template.getId();
        Collection<Statement> oldInterface = store.getReferenceInterface(id);
        Collection<Statement> newInterface = mergeReferenceDefinition(
                oldInterface, diff);
        store.setReferenceInterface(id, newInterface);
        store.setReferenceDefinition(id, newInterface);
    }

    private List<Statement> mergeReferenceDefinition(
            Collection<Statement> data, Collection<Statement> diff) {
        Map<Resource, Map<IRI, List<Value>>> toReplace = new HashMap<>();
        diff.forEach((s) ->
                toReplace.computeIfAbsent(s.getSubject(),
                        (key) -> new HashMap<>())
                        .computeIfAbsent(s.getPredicate(),
                                (key) -> new ArrayList<>())
                        .add(s.getObject()));
        List<Statement> output = new ArrayList<>(diff);
        List<Statement> leftFromOriginal =
                removeWithSubjectAndPredicate(data, diff);
        output.addAll(leftFromOriginal);
        return output;
    }

    private List<Statement> removeWithSubjectAndPredicate(
            Collection<Statement> data, Collection<Statement> toRemove) {
        Map<Resource, Set<IRI>> toDelete = new HashMap<>();
        toRemove.forEach((s) -> {
            toDelete.computeIfAbsent(s.getSubject(), (key) -> new HashSet<>())
                    .add(s.getPredicate());
        });
        // Remove all that are not in the toDelete map.
        return data.stream().filter((s) ->
                !toDelete.getOrDefault(s.getSubject(), Collections.EMPTY_SET)
                        .contains(s.getPredicate())
        ).collect(Collectors.toList());
    }

    public void updateReferenceConfiguration(
            Template template, Collection<Statement> statements)
            throws BaseException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graph = valueFactory.createIRI(
                template.getIri() + "/configuration");
        statements = RdfUtils.forceContext(statements, graph);
        store.setReferenceConfiguration(template.getId(), statements);
    }

    public void removeReference(ReferenceTemplate template)
            throws BaseException {
        templates.remove(template.getIri());
        store.removeReference(template.getId());
    }

    public PluginJarFile getPluginJar(String iri) {
        return plugins.get(iri);
    }

    public Map<String, Template> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    public TemplateStore getStore() {
        return this.store;
    }

}
