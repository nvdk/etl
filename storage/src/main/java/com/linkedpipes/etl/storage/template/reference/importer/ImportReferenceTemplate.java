package com.linkedpipes.etl.storage.template.reference.importer;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.migration.MigrateReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImportReferenceTemplate {

    @FunctionalInterface
    private interface ImportTemplate {

        void apply(ReferenceTemplateWrap template) throws StorageException;

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(ImportReferenceTemplate.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final TemplateApi templatesApi;

    public ImportReferenceTemplate(TemplateApi templatesApi) {
        this.templatesApi = templatesApi;
    }

    /**
     * Just map templates to local does not modify any data.
     */
    public Map<Resource, Resource> mapReferenceTemplates(
            Statements statements) throws StorageException {
        List<ReferenceTemplate> rawTemplates = loadTemplates(statements);
        List<ReferenceTemplateWrap> templates = rawTemplates
                .stream()
                .map(ReferenceTemplateWrap::new)
                .collect(Collectors.toList());
        mapToLocal(templates);
        Map<Resource, Resource> result = new HashMap<>();
        for (ReferenceTemplateWrap template : templates) {
            result.put(template.remoteTemplate, template.template);
        }
        return result;
    }

    /**
     * Map templates to local and import new templates. No existing local
     * is changed.
     */
    public Map<Resource, Resource> importReferenceTemplates(
            Statements statements) throws StorageException {
        List<ReferenceTemplate> rawTemplates = loadTemplates(statements);
        List<ReferenceTemplateWrap> templates = migrateAndWrap(rawTemplates);
        mapToLocal(templates);
        return importTemplates(templates, this::importTemplate);
    }

    protected List<ReferenceTemplate> loadTemplates(Statements statements) {
        return ReferenceTemplateAdapter.asReferenceTemplates(statements);
    }

    protected List<ReferenceTemplateWrap> migrateAndWrap(
            List<ReferenceTemplate> templates) throws StorageException {
        return migrateTemplates(templates)
                .stream()
                .map(ReferenceTemplateWrap::new)
                .collect(Collectors.toList());
    }

    protected List<ReferenceTemplate> migrateTemplates(
            List<ReferenceTemplate> templates) throws StorageException {
        Map<String, String> templateToPlugin = buildPluginTemplates(templates);
        MigrateReferenceTemplate worker = new MigrateReferenceTemplate(iri -> {
            String result = templateToPlugin.get(iri);
            if (result != null) {
                return result;
            }
            if (templatesApi.isPluginTemplate(iri)) {
                return iri;
            }
            return templatesApi.getAncestors(iri).get(0);
        });
        List<ReferenceTemplate> result = new ArrayList<>(templates.size());
        for (ReferenceTemplate template : templates) {
            result.add(worker.migrate(template));
        }
        return result;
    }

    protected Map<String, String> buildPluginTemplates(
            List<ReferenceTemplate> templates) {
        Map<Resource, Resource> parents = new HashMap<>();
        for (ReferenceTemplate template : templates) {
            parents.put(template.resource(), template.template());
        }
        Map<String, String> result = new HashMap<>();
        for (ReferenceTemplate template : templates) {
            Resource parent = template.template();
            while (parents.containsKey(parent)) {
                parent = parents.get(parent);
            }
            result.put(template.resource().stringValue(), parent.stringValue());
        }
        return result;
    }


    protected void mapToLocal(List<ReferenceTemplateWrap> templates)
            throws StorageException {
        Map<Resource, Resource> remoteToLocal = buildLocalMapping();
        for (ReferenceTemplateWrap template : templates) {
            template.resource = remoteToLocal.get(template.resource);
            if (template.resource == null) {
                template.resource = remoteToLocal.get(template.knownAs);
            }
            if (template.template.equals(template.pluginTemplate())) {
                // Template is a plugin level template, there is no need
                // for mapping.
            } else {
                template.template = remoteToLocal.get(template.template);
            }
        }
    }

    protected Map<Resource, Resource> buildLocalMapping()
            throws StorageException {
        Map<Resource, Resource> result = new HashMap<>();
        for (String iri : templatesApi.listReferenceTemplates()) {
            Optional<ReferenceTemplate> templateOptional =
                    templatesApi.loadReferenceTemplate(iri);
            if (templateOptional.isEmpty()) {
                continue;
            }
            ReferenceTemplate template = templateOptional.get();
            result.put(template.resource(), template.resource());
            result.put(template.knownAs(), template.resource());
        }
        return result;
    }

    protected Map<Resource, Resource> importTemplates(
            List<ReferenceTemplateWrap> templates,
            ImportTemplate importCallback) throws StorageException {
        Map<Resource, Resource> result = new HashMap<>();
        boolean templateImported;
        do {
            templateImported = false;
            for (ReferenceTemplateWrap template : templates) {
                if (template.template == null || template.imported) {
                    // Parent is not ready or is already imported.
                    continue;
                }
                importCallback.apply(template);
                updateTemplatesAfterImport(templates, template);
                result.put(template.remoteResource, template.resource);
                template.imported = true;
                templateImported = true;
            }
        } while (templateImported);
        List<ReferenceTemplateWrap> notImported = templates.stream()
                .filter(template -> !template.imported)
                .collect(Collectors.toList());
        reportNotImported(notImported);
        return result;
    }

    protected void importTemplate(
            ReferenceTemplateWrap template) throws StorageException {
        if (template.knownAs == null) {
            // If template is known already we use it's IRI.
            template.knownAs = template.remoteResource;
        }
        if (template.resource == null) {
            // New template that need to be imported.
            ReferenceTemplate storedTemplate =
                    templatesApi.storeReferenceTemplate(
                            template.asReferenceTemplate());
            template.resource = storedTemplate.resource();
        }
    }

    protected void updateTemplatesAfterImport(
            List<ReferenceTemplateWrap> templates,
            ReferenceTemplateWrap imported) {
        for (ReferenceTemplateWrap template : templates) {
            if (template.remoteTemplate.equals(imported.remoteResource)) {
                template.template = imported.resource;
            }
        }
    }

    protected void reportNotImported(List<ReferenceTemplateWrap> templates) {
        if (templates.isEmpty()) {
            return;
        }
        LOG.warn("Can't import all templates.");
        for (ReferenceTemplateWrap template : templates) {
            LOG.info("Template '{}' with parent '{}' was not imported.",
                    template.remoteResource, template.remoteTemplate);
        }
    }

    /**
     * Import new templates and update existing templates.
     */
    public Map<Resource, Resource> importAndUpdateReferenceTemplates(
            Statements statements) throws StorageException {
        List<ReferenceTemplate> rawTemplates = loadTemplates(statements);
        List<ReferenceTemplateWrap> templates = migrateAndWrap(rawTemplates);
        mapToLocal(templates);
        return importTemplates(templates, this::importAndUpdateTemplate);
    }

    protected void importAndUpdateTemplate(
            ReferenceTemplateWrap template) throws StorageException {
        importTemplate(template);
        // Local and we should update it.
        templatesApi.storeReferenceTemplate(
                template.asReferenceTemplate());
    }

}
