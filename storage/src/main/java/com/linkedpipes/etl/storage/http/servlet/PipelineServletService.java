package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.exporter.ExportContent;
import com.linkedpipes.etl.storage.exporter.ExportContentData;
import com.linkedpipes.etl.storage.exporter.ExportOptions;
import com.linkedpipes.etl.storage.exporter.ExportPipeline;
import com.linkedpipes.etl.storage.exporter.ExportTemplate;
import com.linkedpipes.etl.storage.importer.ImportContent;
import com.linkedpipes.etl.storage.importer.ImportOptions;
import com.linkedpipes.etl.storage.importer.ImportOptionsAdapter;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineList;
import com.linkedpipes.etl.storage.pipeline.model.PipelineListItem;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.squash.SquashPipeline;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.eclipse.rdf4j.model.Statement;

import java.nio.channels.Pipe;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PipelineServletService extends ServletService {

    @FunctionalInterface
    private interface PipelineConsumer {

        Response apply(Pipeline pipeline) throws StorageException;

    }

    private final PipelineApi pipelineApi;

    private final TemplateApi templateApi;

    public PipelineServletService(
            PipelineApi pipelineApi,
            TemplateApi templateApi) {
        this.pipelineApi = pipelineApi;
        this.templateApi = templateApi;
    }

    public Response listPipelines(HttpServletRequest request)
            throws StorageException {
        PipelineList list = pipelineApi.createPipelineList();
        Statements content = PipelineAdapter.asRdf(list);
        return responseStatements(request, content);
    }

    public Response pipeline(HttpServletRequest request, String iri)
            throws StorageException {
        return withPipeline(iri, pipeline -> {
            Pipeline result = applyQuery(pipeline, request);
            return responseStatements(request, PipelineAdapter.asRdf(result));
        });
    }

    protected Response withPipeline(String iri, PipelineConsumer callback)
            throws StorageException {
        Optional<Pipeline> pipeline = pipelineApi.loadPipeline(iri);
        if (pipeline.isEmpty()) {
            return responseNotFound();
        }
        return callback.apply(pipeline.get());
    }

    /**
     * Apply URL query options to a pipeline.
     */
    protected Pipeline applyQuery(
            Pipeline pipeline, HttpServletRequest request)
            throws StorageException {
        Pipeline result = pipeline;
        if (shouldRemovePrivateConfiguration(request)) {
            ExportPipeline worker = new ExportPipeline(templateApi);
            result = worker.removePrivateConfiguration(result);
        }
        return result;
    }

    protected boolean shouldRemovePrivateConfiguration(
            HttpServletRequest request) {
        return request.getQueryString().contains("removePrivateConfiguration");
    }

    public Response fullPipeline(HttpServletRequest request, String iri)
            throws StorageException {
        boolean removePrivate = shouldRemovePrivateConfiguration(request);
        ExportContent worker = new ExportContent(
                templateApi, new ExportOptions(removePrivate, true));
        return withPipeline(iri, pipeline -> {
            ExportContentData content = worker.exportPipeline(pipeline);
            Statements result = Statements.arrayList();
            for (Pipeline item : content.pipelines()) {
                result.addAll(PipelineAdapter.asRdf(item));
            }
            for (ReferenceTemplate item : content.referenceTemplates()) {
                result.addAll(ReferenceTemplateAdapter.asRdf(item));
            }
            return responseStatements(request, result);
        });
    }

    public Response squashedPipeline(HttpServletRequest request, String iri)
            throws StorageException {
        return withPipeline(iri, pipeline -> {
            SquashPipeline worker = new SquashPipeline(templateApi);
            Pipeline squashed = worker.squash(pipeline);
            Pipeline result = applyQuery(squashed, request);
            return responseStatements(request, PipelineAdapter.asRdf(result));
        });
    }

    public Response storePipeline(StatementsSelector pipelineRdf)
            throws StorageException {
        List<Pipeline> pipelines = PipelineAdapter.asPipeline(pipelineRdf);
        if (pipelines.isEmpty()) {
            throw new StorageException("Missing pipeline to store.");
        } else if (pipelines.size() > 1) {
            throw new StorageException("Multiple pipelines detected.");
        }
        Pipeline stored = pipelineApi.storePipeline(pipelines.get(0));
        return Response.ok()
                .header("Location", stored.resource().stringValue())
                .build();
    }

    public Response localizePipeline(
            Statements pipelineRdf, Statements optionRdf,
            HttpServletRequest request) throws StorageException {
        ImportContent worker = new ImportContent(pipelineApi, templateApi);
        ImportOptions options = loadOptions(optionRdf);
        List<Pipeline> pipelines = worker.localizeStatements(
                pipelineRdf.selector(), options);
        Statements result = Statements.arrayList();
        for (Pipeline item : pipelines) {
            result.addAll(PipelineAdapter.asRdf(item));
        }
        return responseStatements(request, result);
    }

    public Response importPipeline(
            Statements pipelineRdf, Statements optionRdf,
            HttpServletRequest request) throws StorageException {
        ImportContent worker = new ImportContent(pipelineApi, templateApi);
        ImportOptions options = loadOptions(optionRdf);
        List<Pipeline> pipelines = worker.importStatements(pipelineRdf, options);
        PipelineList list = PipelineAdapter.asPipelineList(pipelines);
        Statements content = PipelineAdapter.asRdf(list);
        return responseStatements(request, content);
    }

    private ImportOptions loadOptions(Statements statements)
            throws StorageException {
        if (statements == null) {
            return defaultImportOptions();
        }
        List<ImportOptions> options = ImportOptionsAdapter.asImportOptions(
                statements.selector());
        if (options.isEmpty()) {
            return defaultImportOptions();
        } else if (options.size() == 1) {
            return options.get(0);
        } else {
            throw new StorageException("Multiple option object detected.");
        }
    }

    private ImportOptions defaultImportOptions() {
        return new ImportOptions(Collections.emptyMap(), true, false);
    }

    public Response deletePipeline(String iri) throws StorageException {
        pipelineApi.deletePipeline(iri);
        return Response.ok().build();
    }

}
