package com.linkedpipes.etl.storage.pipeline.adapter.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.pipeline.model.PipelineConnection;
import com.linkedpipes.etl.storage.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionFlow;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PipelineToRdf {

    protected static final String TYPE =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static Statements pipelineAsRdf(Pipeline pipeline) {
        StatementsBuilder result = Statements.arrayList().builder();
        result.setDefaultGraph(pipeline.resource());
        //
        result.addIri(pipeline.resource(), TYPE, LP_PIPELINE.PIPELINE);
        result.add(pipeline.resource(),
                SKOS.PREF_LABEL,
                pipeline.label());
        result.add(pipeline.resource(),
                LP.HAS_VERSION,
                pipeline.version());
        result.add(pipeline.resource(),
                SKOS.NOTE,
                pipeline.note());
        //
        result.add(pipeline.resource(),
                LP_PIPELINE.HAS_PROFILE,
                pipeline.executionProfile().resource());
        writeExecutionProfile(pipeline.executionProfile(), result);
        // With next version we can add a link from
        // pipeline to components and connections.
        for (PipelineComponent component : pipeline.components()) {
            writeComponent(component, result);
        }
        for (PipelineConnection connection : pipeline.connections()) {
            if (connection instanceof PipelineDataFlow flow) {
                writeDataFlow(flow, result);
            }
            if (connection instanceof PipelineExecutionFlow flow) {
                writeExecutionFlow(flow, result);
            }
        }
        return result;
    }

    protected static void writeExecutionProfile(
            PipelineExecutionProfile profile, StatementsBuilder statements) {
        statements.addIri(profile.resource(), TYPE, LP_PIPELINE.PROFILE);
        statements.add(profile.resource(),
                LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY,
                profile.rdfRepositoryPolicy());
        statements.add(profile.resource(),
                LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE,
                profile.rdfRepositoryType());
    }

    protected static void writeComponent(
            PipelineComponent definition, StatementsBuilder statements) {
        statements.addIri(definition.resource(), TYPE, LP_PIPELINE.COMPONENT);
        statements.add(definition.resource(),
                SKOS.PREF_LABEL,
                definition.label());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_DESCRIPTION,
                definition.description());
        statements.add(definition.resource(),
                SKOS.NOTE,
                definition.note());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_COLOR,
                definition.color());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                definition.configurationGraph());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_X,
                definition.xPosition());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_Y,
                definition.yPosition());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_TEMPLATE,
                definition.template());
        if (definition.disabled() != null &&
                !definition.disabled().booleanValue()) {
            statements.add(definition.resource(),
                    LP_PIPELINE.HAS_DISABLED,
                    SimpleValueFactory.getInstance().createLiteral(false));
        }
        if (!definition.configuration().isEmpty()) {
            Statements configuration =
                    Statements.wrap(definition.configuration());
            statements.addAll(
                    configuration.withGraph(definition.configurationGraph()));
        }
    }

    protected static void writeDataFlow(
            PipelineDataFlow definition, StatementsBuilder statements) {
        statements.addIri(definition.resource(), TYPE, LP_PIPELINE.CONNECTION);
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_SOURCE_COMPONENT,
                definition.source());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_SOURCE_BINDING,
                definition.sourceBinding());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_TARGET_COMPONENT,
                definition.target());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_TARGET_BINDING,
                definition.targetBinding());
    }

    protected static void writeExecutionFlow(
            PipelineExecutionFlow definition, StatementsBuilder statements) {
        statements.addIri(definition.resource(), TYPE, LP_PIPELINE.RUN_AFTER);
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_SOURCE_COMPONENT,
                definition.source());
        statements.add(definition.resource(),
                LP_PIPELINE.HAS_TARGET_COMPONENT,
                definition.target());
    }

}
