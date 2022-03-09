package com.linkedpipes.etl.storage.exporter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.template.TemplateApi;

import java.util.ArrayList;
import java.util.List;

public class ExportPipeline {

    private final ExportTemplate exportTemplate;

    public ExportPipeline(TemplateApi templatesApi) {
        this.exportTemplate = new ExportTemplate(templatesApi);
    }

    public Pipeline exportPipeline(
            Pipeline pipeline, ExportOptions options)
            throws StorageException {
        Pipeline result = pipeline;
        if (options.removePrivateConfiguration()) {
            result = removePrivateConfiguration(pipeline);
        }
        return result;
    }

    public Pipeline removePrivateConfiguration(Pipeline pipeline)
            throws StorageException {
        List<PipelineComponent> components =
                new ArrayList<>(pipeline.components().size());
        for (PipelineComponent component : pipeline.components()) {
            components.add(new PipelineComponent(
                    component.resource(), component.label(),
                    component.description(), component.note(),
                    component.color(), component.xPosition(),
                    component.yPosition(), component.template(),
                    component.disabled(), component.configurationGraph(),
                    exportTemplate.removePrivateConfiguration(
                            component.configuration(),
                            component.template())
            ));
        }
        return new Pipeline(
                pipeline.resource(), pipeline.label(),
                pipeline.version(), pipeline.note(), pipeline.tags(),
                pipeline.executionProfile(), components,
                pipeline.connections());
    }

}
