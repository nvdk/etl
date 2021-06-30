package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.jar.JarComponent;
import com.linkedpipes.etl.storage.jar.JarFacade;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.migration.MigrateStore;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreService;
import com.sun.source.util.Plugin;
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
public class TemplateManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateManager.class);

    private final JarFacade jarFacade;

    private final Configuration configuration;

    private final Map<String, Template> templates = new HashMap<>();

    private final TemplateStoreService storeService;

    private TemplateStore store;

    @Autowired
    public TemplateManager(
            JarFacade jarFacade,
            Configuration configuration) {
        this.jarFacade = jarFacade;
        this.configuration = configuration;
        this.storeService = new TemplateStoreService(
                configuration.getTemplatesDirectory());
    }

    public TemplateStore getStore() {
        return this.store;
    }

    @PostConstruct
    public void initialize() throws BaseException {
        try {
            storeService.initialize();
            store = storeService.createStore();
            importJarFiles();
            if (storeService.shouldMigrate()) {
                migrate();
            }
            importTemplates();
        } catch (Exception ex) {
            LOG.error("Initialization failed.", ex);
            throw ex;
        }
    }

    private void importJarFiles() {
        ImportFromJarFile copyJarTemplates =
                new ImportFromJarFile(store);
        for (JarComponent item : jarFacade.getJarComponents()) {
            copyJarTemplates.importJarComponent(item);
        }
    }

    private void migrate() throws BaseException {
        TemplateStore source = storeService.createStoreFromInfoFile();
        MigrateStore migration =
                new MigrateStore(source, store, storeService.getStoreInfo());
        StoreInfo newStoreInfo = migration.migrate();
        storeService.setStoreInfo(newStoreInfo);
    }

    private void importTemplates() throws StoreException {
        TemplateLoader loader = new TemplateLoader(store);
        for (String id : store.getPluginIdentifiers()) {
            try {
                PluginTemplate template = loader.loadPluginTemplate(id);
                if (template.getIri() == null) {
                    LOG.error("Invalid template ignored: {}", id);
                    continue;
                }
                templates.put(template.getIri(), template);
            } catch (Exception ex) {
                LOG.error("Can't load template: {}", id, ex);
            }
        }
        List<ReferenceTemplate> referenceTemplates = new ArrayList<>();
        for (String id : store.getReferenceIdentifiers()) {
            try {
                ReferenceTemplate template = loader.loadReferenceTemplate(id);
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

    private void setTemplateCoreReferences(List<ReferenceTemplate> templates) {
        for (ReferenceTemplate template : templates) {
            template.setCoreTemplate(findCoreTemplate(template));
        }
    }

    private PluginTemplate findCoreTemplate(ReferenceTemplate template) {
        while (true) {
            Template parent = templates.get(template.getTemplate());
            if (parent == null) {
                LOG.error("Missing parent for: {}", template.getIri());
                return null;
            }
            if (parent.isPlugin()) {
                return (PluginTemplate) parent;
            } else if (parent.isReference()) {
                template = (ReferenceTemplate) parent;
            } else {
                LOG.error("Invalid template type: {}", parent.getIri());
                return null;
            }
        }
    }

    public Map<String, Template> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    public Template createReferenceTemplate(
            Collection<Statement> interfaceRdf,
            Collection<Statement> configurationRdf)
            throws BaseException {
        String id = store.reserveIdentifier();
        String iri = configuration.getDomainName()
                + "/resources/components/" + id;
        ReferenceFactory factory = new ReferenceFactory(store);
        try {
            ReferenceTemplate template = factory.create(
                    interfaceRdf, configurationRdf, id, iri);
            template.setCoreTemplate(findCoreTemplate(template));
            templates.put(template.getIri(), template);
            return template;
        } catch (BaseException ex) {
            store.removeReference(id);
            throw ex;
        }
    }

    public void updateReferenceTemplateInterface(
            ReferenceTemplate template, Collection<Statement> diff)
            throws BaseException {
        diff = RdfUtils.forceContext(diff, template.getIri());
        String id = template.getId();
        Collection<Statement> oldInterface = store.getReferenceInterface(id);
        Collection<Statement> newInterface = update(oldInterface, diff);
        store.setReferenceInterface(id, newInterface);
        store.setReferenceDefinition(id, newInterface);
    }

    private List<Statement> update(
            Collection<Statement> data, Collection<Statement> diff) {
        Map<Resource, Map<IRI, List<Value>>> toReplace = new HashMap<>();
        diff.forEach((s) ->
                toReplace.computeIfAbsent(s.getSubject(),
                        (key) -> new HashMap<>())
                        .computeIfAbsent(s.getPredicate(),
                                (key) -> new ArrayList<>())
                        .add(s.getObject()));
        List<Statement> output = new ArrayList<>();
        output.addAll(diff);
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

}
