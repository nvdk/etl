package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.migration.MigrateV1ToV2;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ImportTemplates {

    private static final Logger LOG =
            LoggerFactory.getLogger(ImportTemplates.class);

    private final TemplateFacade templateFacade;

    private boolean importMissing = false;

    private boolean updateExisting = false;

    private int pipelineVersion;

    private Resource pipelineGraph;

    private Map<IRI, List<Statement>> graphs;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public ImportTemplates(TemplateFacade templateFacade) {
        this.templateFacade = templateFacade;
    }

    public void setImportMissing(boolean importMissing) {
        this.importMissing = importMissing;
    }

    public void setUpdateExisting(boolean updateExisting) {
        this.updateExisting = updateExisting;
    }

    public void setPipelineVersion(int pipelineVersion) {
        this.pipelineVersion = pipelineVersion;
    }

    /**
     * Align templates in the pipeline with local templates. Missing
     * templates are imported.
     */
    public Collection<Statement> importTemplatesFromPipeline(
            Collection<Statement> pipelineRdf) throws BaseException {
        LOG.debug("Import options:");
        LOG.debug("  Import templates: {}", this.importMissing);
        LOG.debug("  Update existing templates: {}", this.updateExisting);
        LOG.debug("  Pipeline version: {}", this.pipelineVersion);
        initialize();
        loadStatements(pipelineRdf);
        if (pipelineGraph == null) {
            // There is no pipeline.
            LOG.warn("No pipeline graph found!");
            return pipelineRdf;
        }
        loadMapping(pipelineRdf);
        importTemplates();
        return collectPipeline();
    }

    private void initialize() {
        this.pipelineGraph = null;
        this.graphs = new HashMap<>();
    }

    private void loadStatements(Collection<Statement> pipelineRdf) {
        for (Statement statement : pipelineRdf) {
            List<Statement> graph = graphs.get(statement.getContext());
            if (graph == null) {
                graph = new LinkedList<>();
                graphs.put((IRI) statement.getContext(), graph);
            }
            if (statement.getPredicate().equals(RDF.TYPE)
                    && statement.getObject().equals(Pipeline.TYPE)) {
                pipelineGraph = statement.getContext();
            }
            graph.add(statement);
        }
    }

    private void loadMapping(Collection<Statement> pipelineRdf) {
        // TODO #884 Import mapping from pipeline.
    }

    private void importTemplates() throws BaseException {
        List<TemplateInfo> templates = TemplateInfo.create(graphs);
        List<TemplateInfo> resolvedTemplates = new ArrayList<>();
        // We try to import templates. As there might be hierarchy we should
        // import at least one template in each cycle.
        while (!templates.isEmpty()) {
            for (TemplateInfo templateInfo : templates) {
                if (resolveTemplate(templateInfo)) {
                    resolvedTemplates.add(templateInfo);
                }
            }
            if (resolvedTemplates.isEmpty()) {
                LOG.error("Failed to import following templates:");
                for (TemplateInfo templateInfo : templates) {
                    LOG.info("   {}", templateInfo.getIri());
                }
                return;
            }
            templates.removeAll(resolvedTemplates);
            resolvedTemplates.clear();
        }
    }

    private boolean resolveTemplate(TemplateInfo template) {
        Template localTemplate;
        // First try to just ask for the URL.
        localTemplate = templateFacade.getTemplate(template.getIri());
        if (localTemplate != null) {
            return true;
        }
        // TODO #884 Check for mapping
        if (importMissing) {
            return importTemplate(template);
        } else {
            return false;
        }
    }

    private void updateLocal(TemplateInfo remote, ReferenceTemplate local)
            throws BaseException {
        templateFacade.updateReferenceInterface(local, remote.getDefinition());
        Template parent = templateFacade.getParent(local);
        prepareTemplateForImport(remote, parent);
        Collection<Statement> config = remote.getConfiguration();
        templateFacade.updateConfiguration(local, config);
    }

    private void prepareTemplateForImport(
            TemplateInfo remote, Template localParent) {
        if (pipelineVersion < 2) {
            Template root = templateFacade.getRootTemplate(localParent);
            if (MigrateV1ToV2.shouldUpdate(root.getIri())) {
                remote.setConfiguration(MigrateV1ToV2.updateConfiguration(
                        remote.getConfiguration(),
                        root.getIri()));
            }
        }
    }

    private boolean importTemplate(TemplateInfo remoteTemplate) {
        Template parent = getLocalParent(remoteTemplate);
        if (parent == null) {
            return false;
        }
        LOG.info("Importing: {} with remote parent: {}",
                remoteTemplate.getIri(), remoteTemplate.getTemplate());
        LOG.info("   local parent: {}", parent.getIri());
        remoteTemplate.setTemplate(valueFactory.createIRI(parent.getIri()));
        prepareTemplateForImport(remoteTemplate, parent);
        try {
            Template template = templateFacade.createReferenceTemplate(
                    remoteTemplate.getDefinition(),
                    remoteTemplate.getConfiguration());
            LOG.info("   imported as : {}", template.getIri());
            // TODO #884 Store template mapping
        } catch (BaseException ex) {
            LOG.error("Can't import template: {}", remoteTemplate.getIri(), ex);
            LOG.info("Template is ignored.");
            return true;
        }
        return true;
    }

    private Template getLocalParent(TemplateInfo remoteTemplate) {
        String parentIri = remoteTemplate.getTemplate().stringValue();
        Template localTemplate = templateFacade.getTemplate(parentIri);
        // TODO #884 localTemplate may be null, check mapping
        return localTemplate;
    }

    private List<Statement> collectPipeline() {
        List<Statement> definition = graphs.get(pipelineGraph);
        Set<Resource> configurations = new HashSet<>();
        List<Statement> toRemove = new ArrayList<>();
        List<Statement> toAdd = new ArrayList<>();
        for (Statement statement : definition) {
            // Check for configuration.
            String predicate = statement.getPredicate().stringValue();
            if (predicate.equals(LP_PIPELINE.HAS_CONFIGURATION_GRAPH)) {
                configurations.add((Resource) statement.getObject());
                continue;
            }
            // Check template references and update them.
            if (predicate.equals(LP_PIPELINE.HAS_TEMPLATE)) {
                // Check for import. Now all templates should be imported
                // so we can just ask for mapping.
                String templateIri = statement.getObject().stringValue();
                String localTemplateIri = getIriForTemplate(templateIri);
                toRemove.add(statement);
                toAdd.add(valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        valueFactory.createIRI(localTemplateIri),
                        statement.getContext()));
            }
        }
        // Collect.
        List<Statement> result = new ArrayList<>(definition.size());
        result.addAll(definition);
        result.removeAll(toRemove);
        result.addAll(toAdd);
        for (Resource configurationIri : configurations) {
            result.addAll(graphs.getOrDefault(
                    configurationIri, Collections.emptyList()));
        }
        return result;
    }

    private String getIriForTemplate(String iri) {
        // TODO #884 Check for mapping
        return iri;
    }

    /**
     * Remove templates and all related information from a pipeline.
     * TODO Move to another class.
     */
    public Collection<Statement> removeTemplates(
            Collection<Statement> pipelineRdf) {
        initialize();
        loadStatements(pipelineRdf);
        // Collect pipeline without templates.
        List<Statement> definition = graphs.get(pipelineGraph);
        Set<Resource> configurations = new HashSet<>();
        for (Statement statement : definition) {
            // Check for configuration.
            String predicate = statement.getPredicate().stringValue();
            if (predicate.equals(LP_PIPELINE.HAS_CONFIGURATION_GRAPH)) {
                configurations.add((Resource) statement.getObject());
            }
        }
        // Collect.
        List<Statement> result = new ArrayList<>(definition.size());
        result.addAll(definition);
        List<Statement> toRemove = new ArrayList<>();
        result.removeAll(toRemove);
        List<Statement> toAdd = new ArrayList<>();
        result.addAll(toAdd);
        for (Resource configurationIri : configurations) {
            result.addAll(graphs.getOrDefault(
                    configurationIri, Collections.emptyList()));
        }
        return result;
    }

}
