package com.linkedpipes.etl.storage.exporter;

import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

import java.util.Collection;

/**
 * Wraps data for export.
 */
public record ExportContentData(
        Collection<Pipeline> pipelines,
        Collection<ReferenceTemplate> referenceTemplates
) {

}
