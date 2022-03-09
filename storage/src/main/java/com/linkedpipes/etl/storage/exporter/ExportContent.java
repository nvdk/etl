package com.linkedpipes.etl.storage.exporter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ExportContent {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExportContent.class);

    private final ExportTemplate exportTemplate;

    private final TemplateApi templatesApi;

    private final ExportOptions options;

    private final Set<String> templatesWithoutAncestors = new HashSet<>();

    public ExportContent(TemplateApi templatesApi, ExportOptions options) {
        this.exportTemplate = new ExportTemplate(templatesApi);
        this.templatesApi = templatesApi;
        this.options = options;
    }

    public ExportContentData exportPipeline(Pipeline pipeline)
            throws StorageException {
        return exportPipelines(List.of(pipeline));
    }

    public ExportContentData exportPipelines(
            List<Pipeline> pipelines) throws StorageException {
        Set<String> templatesForExport = new HashSet<>();
        LOG.info("Collecting templates for export.");
        for (Pipeline pipeline : pipelines) {
            templatesForExport.addAll(collectTemplates(pipeline));
        }
        LOG.info("Exporting templates.");
        List<ReferenceTemplate> templates = new ArrayList<>();
        for (String templateIri : templatesForExport) {
            // Skip templates.
            if (templatesApi.isPluginTemplate(templateIri)) {
                continue;
            }
            Optional<ReferenceTemplate> template =
                    exportTemplate.exportTemplate(templateIri, options);
            if (template.isEmpty()) {
                LOG.warn("Missing template {} for export.", templateIri);
                if (options.ignoreMissingTemplates()) {
                    continue;
                } else {
                    throw new StorageException(
                            "Missing template {} for export.", templateIri);
                }
            }
            templates.add(template.get());
        }
        return new ExportContentData(pipelines, templates);
    }

    /**
     * Return all templates and their ancestors used in a pipeline.
     */
    private Set<String> collectTemplates(Pipeline pipeline)
            throws StorageException {
        Set<String> result = new HashSet<>();
        for (PipelineComponent component : pipeline.components()) {
            String iri = component.template().stringValue();
            if (templatesWithoutAncestors.contains(iri)) {
                // Ignore those that we have troubles loading.
                continue;
            }
            result.add(iri);
            try {
                result.addAll(templatesApi.getAncestors(iri));
            } catch (StorageException ex) {
                LOG.warn("Can't get full ancestor list for {}", iri);
                if (options.ignoreMissingTemplates()) {
                    templatesWithoutAncestors.add(iri);
                } else {
                    throw ex;
                }
            }
        }
        return result;
    }

}
