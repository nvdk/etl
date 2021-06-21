package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.jar.JarComponent;
import com.linkedpipes.etl.storage.jar.JarFacade;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.migration.MigrateStore;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreService;
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
        List<ReferenceTemplate> referenceTemplates = new ArrayList<>();
        for (RepositoryReference reference : store.getReferences()) {
            try {
                Template template = loader.loadTemplate(reference);
                if (template.getIri() == null) {
                    LOG.error("Invalid template ignored: {}",
                            reference.getId());
                    continue;
                }
                if (template instanceof ReferenceTemplate) {
                    referenceTemplates.add((ReferenceTemplate) template);
                }
                templates.put(template.getIri(), template);
            } catch (Exception ex) {
                LOG.error("Can't load template: {}", reference.getId(), ex);
            }
        }
        setTemplateCoreReferences(referenceTemplates);
    }

    private void setTemplateCoreReferences(List<ReferenceTemplate> templates) {
        for (ReferenceTemplate template : templates) {
            template.setCoreTemplate(findCoreTemplate(template));
        }
    }

    private JarTemplate findCoreTemplate(ReferenceTemplate template) {
        while (true) {
            Template parent = templates.get(template.getTemplate());
            if (parent == null) {
                LOG.error("Missing parent for: {}", template.getIri());
                return null;
            }
            switch (parent.getType()) {
                case JAR_TEMPLATE:
                    return (JarTemplate) parent;
                case REFERENCE_TEMPLATE:
                    template = (ReferenceTemplate) parent;
                    break;
                default:
                    LOG.error("Invalid template type: {}",
                            parent.getIri());
                    return null;
            }
        }
    }


    public Map<String, Template> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    public Template createTemplate(
            Collection<Statement> interfaceRdf,
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf)
            throws BaseException {
        String id = store.reserveIdentifier();
        String iri = configuration.getDomainName()
                + "/resources/components/" + id;
        ReferenceFactory factory = new ReferenceFactory(store);
        try {
            ReferenceTemplate template = factory.create(
                    interfaceRdf, configurationRdf,
                    descriptionRdf, id, iri);
            template.setCoreTemplate(findCoreTemplate(template));
            templates.put(template.getIri(), template);
            return template;
        } catch (BaseException ex) {
            store.remove(RepositoryReference.createReference(id));
            throw ex;
        }
    }

    public void updateTemplateInterface(
            Template template, Collection<Statement> diff)
            throws BaseException {
        if (template.getType() != Template.Type.REFERENCE_TEMPLATE) {
            throw new BaseException("Only reference templates can be updated");
        }
        diff = RdfUtils.forceContext(diff, template.getIri());
        Collection<Statement> newInterface =
                update(store.getInterface(template), diff);
        store.setInterface(template, newInterface);
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

    public void updateConfig(
            Template template, Collection<Statement> statements)
            throws BaseException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graph = valueFactory.createIRI(
                template.getIri() + "/configuration");
        statements = RdfUtils.forceContext(statements, graph);
        store.setConfig(template, statements);
    }

    public void remove(Template template) throws BaseException {
        if (template.getType() != Template.Type.REFERENCE_TEMPLATE) {
            throw new BaseException("Can't delete non-reference template: {}",
                    template.getIri());
        }
        templates.remove(template.getIri());
        store.remove(template);
    }

}
