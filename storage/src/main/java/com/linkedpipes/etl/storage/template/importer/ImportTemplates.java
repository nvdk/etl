package com.linkedpipes.etl.storage.template.importer;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.etl.storage.template.migration.MigrateTemplate;
import com.linkedpipes.etl.storage.template.migration.TemplateReader;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainerFactory;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinitionAdapter;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given pipeline definition import templates.
 */
public class ImportTemplates {

    /**
     * We use this to store some basic information about a template.
     */
    private static class ReferenceTemplateBasics {

        public final Resource resource;

        public final Resource graph;

        public Resource parent;

        public final Resource configuration;

        public final Integer version;

        public Resource root = null;

        public Resource knownAs = null;

        /**
         * If set the given template has a local version.
         */
        public Resource localResource = null;

        public Statements definitionStatements;

        public Statements configurationStatements;

        public ReferenceTemplateBasics(
                Resource resource, Resource graph,
                Resource parent, Resource configuration, Integer version) {
            this.resource = resource;
            this.graph = graph;
            this.parent = parent;
            this.configuration = configuration;
            this.version = version;
        }
    }

    private static final Resource MAPPING_GRAPH =
            SimpleValueFactory.getInstance().createIRI(
                    "http://etl.linkedpipes.com/resources/plugins/mapping");

    private final TemplateEventListener listener;

    private final TemplateStore store;

    private final ImportSources.PluginSource pluginSource;

    private final ImportSources.TemplateSource templateSource;

    /**
     * Domain name, must not end on "/".
     */
    private final String templateDomain;

    public ImportTemplates(
            TemplateEventListener listener,
            TemplateStore store,
            String templateDomain,
            ImportSources.PluginSource pluginSource,
            ImportSources.TemplateSource templateSource) {
        this.listener = listener;
        this.store = store;
        this.templateDomain = templateDomain;
        this.pluginSource = pluginSource;
        this.templateSource = templateSource;
    }

    /**
     * Import all new templates and update configurations of all local
     * templates.
     */
    public ImportTemplatesResults importFromStatement(
            Collection<Statement> statements, int defaultVersion)
            throws TemplateImportException {
        Map<Resource, Statements> graphs =
                Statements.wrap(statements).splitByGraph();
        List<ReferenceTemplateBasics> templates = loadTemplatesData(
                graphs, defaultVersion);
        orderForImport(templates);
        return importOrderedTemplates(graphs, templates);
    }

    private List<ReferenceTemplateBasics> loadTemplatesData(
            Map<Resource, Statements> graphs, int defaultVersion)
            throws TemplateImportException {
        List<ReferenceTemplateBasics> result =
                extractTemplatesData(graphs, defaultVersion);
        loadKnowAsToTemplates(graphs, result);
        loadTemplateRoots(result);
        loadLocalResourceToTemplates(result);
        return result;
    }

    private List<ReferenceTemplateBasics> extractTemplatesData(
            Map<Resource, Statements> graphs, int defaultVersion) {
        List<ReferenceTemplateBasics> result = new ArrayList<>();
        for (Map.Entry<Resource, Statements> entry : graphs.entrySet()) {
            Statements statements = entry.getValue();
            Resource resource = TemplateReader.readResource(statements);
            if (resource == null) {
                continue;
            }
            Resource parent = TemplateReader.readParent(resource, statements);
            Resource configuration = TemplateReader.readConfiguration(
                    resource, statements);
            Integer version = TemplateReader.readVersion(
                    resource, statements, defaultVersion);
            result.add(new ReferenceTemplateBasics(
                    resource, entry.getKey(), parent, configuration, version
            ));
        }
        return result;
    }

    /**
     * We need to load from templates as well as from the mapping graph.
     */
    private void loadKnowAsToTemplates(
            Map<Resource, Statements> graphs,
            List<ReferenceTemplateBasics> templates) {
        // Map from imported to knownAs.
        Map<Resource, Resource> mapping = new HashMap<>();
        Statements mappingGraph = graphs.get(MAPPING_GRAPH);
        if (mappingGraph != null) {
            mappingGraph
                    .select(null, "http://www.w3.org/2002/07/owl#sameAs", null)
                    .stream()
                    .filter(statement -> statement.getObject().isResource())
                    .forEach(statement -> mapping.put(
                            (Resource) statement.getObject(),
                            statement.getSubject()));
        }
        for (ReferenceTemplateBasics template : templates) {
            Resource knownAs = TemplateReader.readKnownAs(
                    template.resource, graphs.get(template.graph));
            if (knownAs != null) {
                template.knownAs = knownAs;
            } else {
                template.knownAs = mapping.get(template.resource);
            }
        }
    }

    private void loadLocalResourceToTemplates(
            List<ReferenceTemplateBasics> templates)
            throws TemplateImportException {
        for (ReferenceTemplateBasics template : templates) {
            template.localResource = getLocalResource(template);
        }
    }

    private Resource getLocalResource(
            ReferenceTemplateBasics template) throws TemplateImportException {
        try {
            Resource knownAs = template.knownAs;
            // If the knownAs is set we try to use it to resolve the
            // template mapping.
            if (knownAs != null) {
                if (this.pluginSource.isPluginTemplate(knownAs.stringValue())) {
                    // The template is known as a local plugin.
                    return knownAs;
                }
                String parent = this.templateSource.getKnownAs(
                        knownAs.stringValue());
                if (parent != null) {
                    // The imported template and a local template has the same
                    // known as value.
                    return SimpleValueFactory.getInstance().createIRI(parent);
                }
            }
            if (this.pluginSource.isPluginTemplate(template.resource.stringValue())) {
                // The template is local plugin.
                return template.resource;
            }
            String instance = this.templateSource.getKnownAs(
                    template.resource.stringValue());
            if (instance != null) {
                // There is a local template that is known as the imported
                // template.
                return SimpleValueFactory.getInstance().createIRI(instance);
            }

        } catch (BaseException ex) {
            throw new TemplateImportException(
                    "Can't read local templates.");
        }
        return null;
    }

    private void loadTemplateRoots(List<ReferenceTemplateBasics> templates)
            throws TemplateImportException {
        Map<Resource, Resource> roots = new HashMap<>();
        for (ReferenceTemplateBasics template : templates) {
            if (template.root != null) {
                if (isPluginTemplate(template.root)) {
                    roots.put(template.resource, template.root);
                    continue;
                }
                throw new TemplateImportException(
                        "Defined root is not available.");
            }
            if (isPluginTemplate(template.parent)) {
                template.root = template.parent;
                roots.put(template.resource, template.root);
            }
        }
        // We need to propagate roots to other templates.
        while (roots.size() < templates.size()) {
            boolean changed = false;
            for (ReferenceTemplateBasics template : templates) {
                if (template.root != null) {
                    continue;
                }
                Resource root = roots.get(template.parent);
                if (root == null) {
                    continue;
                }
                template.root = root;
                roots.put(template.resource, template.root);
                changed = true;
            }
            if (!changed) {
                throw new TemplateImportException(
                        "Can't determine template roots.");
            }
        }
    }

    private boolean isPluginTemplate(
            Resource resource) throws TemplateImportException {
        if (resource == null) {
            return false;
        }
        try {
            return pluginSource.isPluginTemplate(resource.stringValue());
        } catch (BaseException ex) {
            throw new TemplateImportException(
                    "Can't determine parent type.", ex);
        }
    }

    /**
     * Order given list so the templates can be imported. We know that
     * the templates can inherit one from another, but there is never a cycle.
     * The only issue can be if we are missing some core templates, in such
     * case the import fail.
     */
    private void orderForImport(
            List<ReferenceTemplateBasics> templates)
            throws TemplateImportException {
        Map<Resource, Integer> ordering = new HashMap<>();
        // Sed with templates that has a plugin template as a parent.
        for (ReferenceTemplateBasics template : templates) {
            if (template.parent == template.root) {
                ordering.put(template.resource, 0);
            }
        }
        // Create ordering.
        boolean hasChanged = false;
        while (ordering.size() < templates.size()) {
            for (ReferenceTemplateBasics template : templates) {
                if (ordering.containsKey(template.resource)) {
                    continue;
                }
                Integer parentOrder = ordering.get(template.parent);
                if (parentOrder == null) {
                    continue;
                }
                ordering.put(template.resource, parentOrder + 1);
                hasChanged = true;
            }
            if (hasChanged) {
                hasChanged = false;
            } else {
                throw new TemplateImportException(
                        "Can not determine order of template for import.");
            }
        }
        // Order templates using the created ordering.
        templates.sort(Comparator.comparingInt(
                template -> ordering.get(template.resource)));
    }

    /**
     * Import all new templates and update configurations of all local
     * templates.
     */
    private ImportTemplatesResults importOrderedTemplates(
            Map<Resource, Statements> graphs,
            List<ReferenceTemplateBasics> templates)
            throws TemplateImportException {
        // As we import templates, we need to keep a place where to update
        // the parent IRIs.
        Map<Resource, Resource> templateMap = new HashMap<>();
        migrateTemplates(graphs, templates);
        ImportTemplatesResults result = new ImportTemplatesResults();
        for (ReferenceTemplateBasics template : templates) {
            // The parent may not be available.
            template.parent = templateMap.getOrDefault(
                    template.parent, template.parent);
            if (template.localResource == null) {
                // We need to import new template.
                template.localResource = importTemplate(template);
                result.localizedTemplates.put(
                        template.resource, template.localResource);
            } else {
                // Update existing template.
                updateTemplate(template);
                result.updatedTemplates.add(template.localResource);
            }
            templateMap.put(template.resource, template.localResource);
        }
        return result;
    }

    /**
     * Migrate all given templates.
     */
    private void migrateTemplates(
            Map<Resource, Statements> graphs,
            List<ReferenceTemplateBasics> templates)
            throws TemplateImportException {
        Map<String, ReferenceTemplateBasics> templatesMap = new HashMap<>();
        for (ReferenceTemplateBasics template : templates) {
            templatesMap.put(template.resource.stringValue(), template);
        }
        MigrateTemplate migration = new MigrateTemplate(
                (iri) -> templatesMap.get(iri).root.stringValue(),
                (iri) -> {
                    ReferenceTemplateBasics template = templatesMap.get(iri);
                    if (template == null || template.knownAs == null) {
                        return null;
                    }
                    return template.knownAs.stringValue();
                });
        // We need to migrate all templates first, one at a time. After
        // migration we can perform modifications on a the local instance.
        for (ReferenceTemplateBasics template : templates) {
            ReferenceContainer container = new ReferenceContainer();
            container.resource = template.resource;
            container.definitionStatements =
                    graphs.get(template.graph);
            container.configurationStatements =
                    graphs.get(template.configuration);
            //
            ReferenceContainer migratedContainer;
            try {
                migratedContainer = migration.migrateReferenceTemplate(
                        container, template.version);
            } catch (BaseException ex) {
                throw new TemplateImportException(
                        "Can't migrate imported template");
            }
            template.definitionStatements =
                    migratedContainer.definitionStatements;
            template.configurationStatements =
                    migratedContainer.configurationStatements;
        }
    }

    private Resource importTemplate(ReferenceTemplateBasics template)
            throws TemplateImportException {
        // Reserve IRI.
        String iri;
        try {
            iri = store.reserveIri(this.templateDomain);
        } catch (BaseException ex) {
            throw new TemplateImportException("Can't reserve template.", ex);
        }
        // Convert to the given IRI.
        ReferenceContainer localizedContainer;
        try {
            localizedContainer = createLocalizedContainer(iri, template);
        } catch (TemplateImportException ex) {
            store.silentRemoveReference(iri);
            throw ex;
        }
        // Store data.
        try {
            store.setReference(
                    iri,
                    localizedContainer.definitionStatements,
                    localizedContainer.configurationStatements);
        } catch (StoreException ex) {
            store.silentRemoveReference(iri);
            throw new TemplateImportException(
                    "Can't create local template.", ex);
        }
        // Notify listeners.
        listener.onReferenceTemplateCreated(localizedContainer);
        //
        return SimpleValueFactory.getInstance().createIRI(iri);
    }

    private ReferenceContainer createLocalizedContainer(
            String iri, ReferenceTemplateBasics template)
            throws TemplateImportException {
        ReferenceDefinition definition = ReferenceDefinitionAdapter.create(
                template.definitionStatements);
        if (definition == null) {
            throw new TemplateImportException("Missing reference template.");
        }
        definition.template = template.parent;
        definition.root = template.root;
        // knowAs may be stored in extra graph, to make sure we just set
        // it here. If it is not set we use the imported template resource.
        definition.knownAs = template.knownAs == null ?
                template.resource : template.knownAs;
        return (new ReferenceContainerFactory()).create(
                iri, definition, template.configurationStatements);
    }

    /**
     * Update local template.
     */
    private void updateTemplate(
            ReferenceTemplateBasics template)
            throws TemplateImportException {
        String iri = template.localResource.stringValue();
        // Convert to the given IRI.
        ReferenceContainer container = createLocalizedContainer(iri, template);
        // Store the data.
        ReferenceDefinition oldDefinition;
        try {
            oldDefinition = ReferenceDefinitionAdapter.create(
                    store.getReferenceDefinition(iri));
        } catch (StoreException ex) {
            throw new TemplateImportException(
                    "Can't read old definition.", ex);
        }
        try {
            store.setReference(
                    iri,
                    container.definitionStatements,
                    container.configurationStatements);
        } catch (StoreException ex) {
            throw new TemplateImportException(
                    "Can't create local template.", ex);
        }
        // Notify listeners.
        listener.onReferenceTemplateChanged(
                oldDefinition, container.definition);
        listener.onReferenceTemplateConfigurationChanged(
                container.resource.stringValue(),
                container.configurationStatements);
    }

    /**
     * Return mapping from given templates to local templates if they exists.
     */
    public ImportTemplatesResults mapFromStatement(
            Collection<Statement> statements, int defaultVersion) {
        Map<Resource, Statements> graphs =
                Statements.wrap(statements).splitByGraph();
        List<ReferenceTemplateBasics> templates = extractTemplatesData(
                graphs, defaultVersion);
        ImportTemplatesResults result = new ImportTemplatesResults();
        for (ReferenceTemplateBasics template : templates) {
            if (template.localResource == null) {
                result.ignoredTemplates.add(template.resource);
            } else {
                result.localizedTemplates.put(
                        template.resource, template.localResource);
            }
        }
        return result;
    }

}
