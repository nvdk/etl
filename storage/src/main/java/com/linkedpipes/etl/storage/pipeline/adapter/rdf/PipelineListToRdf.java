package com.linkedpipes.etl.storage.pipeline.adapter.rdf;

import com.linkedpipes.etl.model.vocabulary.RDF;
import com.linkedpipes.etl.storage.pipeline.model.PipelineList;
import com.linkedpipes.etl.storage.pipeline.model.PipelineListItem;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.Literal;

public class PipelineListToRdf {

    private static final String PIPELINE_ITEM =
            "http://etl.linkedpipes.com/ontology/PipelineListItem";

    private static final String HAS_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String HAS_TAG =
            "http://etl.linkedpipes.com/ontology/tag";

    public static Statements pipelineListAsRdf(PipelineList pipelines) {
        StatementsBuilder result = Statements.arrayList().builder();
        for (PipelineListItem item : pipelines.pipelines()) {
            result.addIri(item.resource(), RDF.TYPE, PIPELINE_ITEM);
            result.add(item.resource(), HAS_LABEL, item.label());
            for (Literal tag : item.tags()) {
                result.add(item.resource(), HAS_TAG, tag);
            }
        }
        return result;
    }

}
