package com.linkedpipes.etl.storage.importer;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.migration.MigratePipeline;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.reference.importer.ImportReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.migration.PluginTemplateSource;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Perform pipeline modifications (migration, update) based on given
 * options and return modified pipeline, may store and modify templates.
 */
public class ImportContent {

    private final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    private final PipelineApi pipelineApi;

    private final TemplateApi templatesApi;

    private final PluginTemplateSource templateSource;

    public ImportContent(
            PipelineApi pipelineApi, TemplateApi templatesApi) {
        this.pipelineApi = pipelineApi;
        this.templatesApi = templatesApi;
        this.templateSource = this::getPluginTemplate;
    }

    private String getPluginTemplate(String template) throws StorageException {
        String parent = template;
        Optional<String> parentNext = templatesApi.getParent(parent);
        while (parentNext.isPresent()) {
            parent = parentNext.get();
            parentNext = templatesApi.getParent(parent);
        }
        return parent;
    }

    /**
     * Localize and import all.
     */
    public List<Pipeline> importStatements(
            Statements statements, ImportOptions importOptions)
            throws StorageException {
        StatementsSelector statementsSelector = statements.selector();
        statementsSelector.buildResourceIndex();
        List<Pipeline> pipelines =
                localizeStatements(statementsSelector, importOptions);
        return importPipelines(pipelines);
    }

    /**
     * Localize pipelines and import templates. The aim is to allow
     * components from given pipeline to be used on this instance.
     */
    public List<Pipeline> localizeStatements(
            StatementsSelector statements, ImportOptions importOptions)
            throws StorageException {
        Map<Resource, Resource> mapping =
                loadTemplates(statements, importOptions);
        List<Pipeline> result = new ArrayList<>();
        Map<Resource, ImportOptions.PipelineOptions> options =
                importOptions.pipelines();
        for (Pipeline rawPipeline : PipelineAdapter.asPipeline(statements)) {
            Pipeline pipeline = updateTemplates(rawPipeline, mapping);
            pipeline = migratePipeline(pipeline);
            pipeline = applyOptions(pipeline, options.get(pipeline.resource()));
            result.add(pipeline);
        }
        return result;
    }

    private Map<Resource, Resource> loadTemplates(
            Statements statements, ImportOptions options)
            throws StorageException {
        ImportReferenceTemplate worker = new ImportReferenceTemplate(
                this.templatesApi);
        if (options.importNewTemplates()) {
            if (options.updateLocalTemplates()) {
                return worker.importReferenceTemplates(statements);
            } else {
                return worker.importAndUpdateReferenceTemplates(statements);
            }
        } else {
            return worker.mapReferenceTemplates(statements);
        }
    }

    private Pipeline updateTemplates(
            Pipeline pipeline, Map<Resource, Resource> mapping) {
        List<PipelineComponent> components = new ArrayList<>(
                pipeline.components().size());
        for (PipelineComponent component : pipeline.components()) {
            Resource template = mapping.getOrDefault(
                    component.template(), component.template());
            components.add(new PipelineComponent(
                    component.resource(),
                    component.label(),
                    component.description(),
                    component.note(),
                    component.color(),
                    component.xPosition(),
                    component.yPosition(),
                    template,
                    component.disabled(),
                    component.configurationGraph(),
                    component.configuration()));
        }
        return new Pipeline(
                pipeline.resource(),
                pipeline.label(),
                pipeline.version(),
                pipeline.note(),
                pipeline.tags(),
                pipeline.executionProfile(),
                components,
                pipeline.connections());
    }

    private Pipeline migratePipeline(Pipeline pipeline)
            throws StorageException {
        MigratePipeline migratePipeline = new MigratePipeline(templateSource);
        return migratePipeline.migratePipeline(pipeline);
    }

    private Pipeline applyOptions(
            Pipeline pipeline, ImportOptions.PipelineOptions options)
            throws StorageException {
        if (options == null) {
            // We just set root to a blank node.
            return new Pipeline(
                    valueFactory.createBNode(),
                    pipeline.label(), pipeline.version(), pipeline.note(),
                    pipeline.tags(), pipeline.executionProfile(),
                    pipeline.components(), pipeline.connections());
        }
        Literal label = pipeline.label();
        if (options.label() != null) {
            label = options.label();
        }
        Resource resource = null;
        if (options.suffix() != null) {
            resource = valueFactory.createIRI(
                    pipelineApi.reservePipelineIri(options.suffix()));
        } else if (shouldUpdateResources(options)) {
            resource = valueFactory.createIRI(
                    pipelineApi.reservePipelineIri(null));
        }
        pipeline = new Pipeline(
                resource, label,
                pipeline.version(), pipeline.note(),
                pipeline.tags(), pipeline.executionProfile(),
                pipeline.components(), pipeline.connections());
        if (shouldUpdateResources(options)) {
            pipeline = AlignPipelineResources.apply(pipeline);
        }
        return pipeline;
    }

    private boolean shouldUpdateResources(
            ImportOptions.PipelineOptions options) {
        return options.updateResources();
    }

    private List<Pipeline> importPipelines(List<Pipeline> pipelines)
            throws StorageException {
        List<Pipeline> result = new ArrayList<>();
        for (Pipeline pipeline : pipelines) {
            Pipeline storedPipeline = pipelineApi.storePipeline(pipeline);
            result.add(storedPipeline);
        }
        return result;
    }

}

