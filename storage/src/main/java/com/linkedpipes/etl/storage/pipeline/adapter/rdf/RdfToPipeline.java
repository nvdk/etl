package com.linkedpipes.etl.storage.pipeline.adapter.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.pipeline.model.PipelineConnection;
import com.linkedpipes.etl.storage.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionFlow;
import com.linkedpipes.etl.storage.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RdfToPipeline {

    private static final IRI TYPE =
            SimpleValueFactory.getInstance().createIRI(
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    private static final Resource PIPELINE =
            SimpleValueFactory.getInstance().createIRI(LP_PIPELINE.PIPELINE);

    public static List<Pipeline> asPipelines(StatementsSelector statements) {
        return statements.selectSubjects(TYPE, PIPELINE)
                .stream().map(statement -> loadPipeline(
                        statements,
                        statement.getSubject(),
                        statement.getContext()))
                .collect(Collectors.toList());
    }

    private static Pipeline loadPipeline(
            StatementsSelector statements,
            Resource resource, Resource graph) {
        Literal label = null, version = null, note = null;
        List<Literal> tags = new ArrayList<>();
        PipelineExecutionProfile profile = null;

        for (Statement statement : statements.withResource(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case SKOS.PREF_LABEL:
                    if (value.isLiteral()) {
                        label = (Literal) value;
                    }
                    break;
                case LP.HAS_VERSION:
                    if (value.isLiteral()) {
                        version = (Literal) value;
                    }
                    break;
                case SKOS.NOTE:
                    if (value.isLiteral()) {
                        note = (Literal) value;
                    }
                    break;
                case LP_PIPELINE.HAS_TAG:
                    if (value.isLiteral()) {
                        tags.add((Literal) value);
                    }
                    break;
                case LP_PIPELINE.HAS_PROFILE:
                    if (value.isResource()) {
                        profile = loadExecutionProfile(
                                statements, (Resource) value);
                    }
                    break;
                default:
                    break;
            }
        }
        // There is no connection from pipeline to components and
        // connections, so we load that by type.
        StatementsSelector pipelineGraph =
                statements.selectByGraph(graph).selector();
        List<PipelineComponent> components = new ArrayList<>();
        for (Resource subject : pipelineGraph.selectSubjectsWithType(
                LP_PIPELINE.COMPONENT).subjects()) {
            components.add(loadComponent(statements, subject));
        }
        List<PipelineConnection> connections = new ArrayList<>();
        for (Resource subject : pipelineGraph.selectSubjectsWithType(
                LP_PIPELINE.RUN_AFTER).subjects()) {
            connections.add(loadExecutionFlow(statements, subject));
        }
        for (Resource subject : pipelineGraph.selectSubjectsWithType(
                LP_PIPELINE.CONNECTION).subjects()) {
            connections.add(loadDataFlow(statements, subject));
        }

        profile = sanitizeProfile(profile, resource);

        return new Pipeline(
                resource, label, version, note, tags,
                profile, components, connections);
    }

    private static PipelineExecutionProfile loadExecutionProfile(
            StatementsSelector statements, Resource profileResource) {
        Resource rdfRepositoryPolicy = null, rdfRepositoryType = null;
        for (Statement statement : statements.withResource(profileResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY:
                    if (value.isResource()) {
                        rdfRepositoryPolicy = (Resource) value;
                    }
                    break;
                case LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE:
                    if (value.isResource()) {
                        rdfRepositoryType = (Resource) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return new PipelineExecutionProfile(
                profileResource, rdfRepositoryPolicy, rdfRepositoryType);
    }

    private static PipelineComponent loadComponent(
            StatementsSelector statements, Resource componentResource) {
        Literal label = null, description = null, note = null,
                x = null, y = null, disabled = null;
        Value color = null;
        Resource template = null;
        IRI configurationGraph = null;

        for (Statement statement : statements.withResource(componentResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case SKOS.PREF_LABEL:
                    if (value.isLiteral()) {
                        label = (Literal) value;
                    }
                    break;
                case LP_PIPELINE.HAS_DESCRIPTION:
                    if (value.isLiteral()) {
                        description = (Literal) value;
                    }
                    break;
                case SKOS.NOTE:
                    if (value.isLiteral()) {
                        note = (Literal) value;
                    }
                    break;
                case LP_PIPELINE.HAS_COLOR:
                    color = value;
                    break;
                case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                    if (value.isIRI()) {
                        configurationGraph = (IRI) value;
                    }
                    break;
                case LP_PIPELINE.HAS_X:
                    if (value.isLiteral()) {
                        x = (Literal) value;
                    }
                    break;
                case LP_PIPELINE.HAS_Y:
                    if (value.isLiteral()) {
                        y = (Literal) value;
                    }
                case LP_PIPELINE.HAS_TEMPLATE:
                    if (value.isResource()) {
                        template = (Resource) value;
                    }
                    break;
                case LP_PIPELINE.HAS_DISABLED:
                    if (value.isLiteral()) {
                        disabled = (Literal) value;
                    }
                default:
                    break;
            }
        }
        return new PipelineComponent(
                componentResource, label, description, note, color,
                x, y, template, disabled, configurationGraph,
                statements.selectByGraph(configurationGraph)
        );
    }

    private static PipelineDataFlow loadDataFlow(
            StatementsSelector statements, Resource flowResource) {
        Resource source = null, target = null;
        Value sourceBinding = null, targetBinding = null;
        for (Statement statement : statements.withResource(flowResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_SOURCE_BINDING:
                    sourceBinding = value;
                    break;
                case LP_PIPELINE.HAS_TARGET_BINDING:
                    targetBinding = value;
                    break;
                case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                    if (value.isResource()) {
                        source = (Resource) value;
                    }
                    break;
                case LP_PIPELINE.HAS_TARGET_COMPONENT:
                    if (value.isResource()) {
                        target = (Resource) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return new PipelineDataFlow(
                flowResource, source, target,
                sourceBinding, targetBinding);
    }

    private static PipelineExecutionFlow loadExecutionFlow(
            StatementsSelector statements, Resource flowResource) {
        Resource source = null, target = null;
        for (Statement statement : statements.withResource(flowResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                    if (value.isResource()) {
                        source = (Resource) value;
                    }
                    break;
                case LP_PIPELINE.HAS_TARGET_COMPONENT:
                    if (value.isResource()) {
                        target = (Resource) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return new PipelineExecutionFlow(flowResource, source, target);
    }

    /**
     * Make sure execution profile exists. In addition, profile
     * resource is determined by pipeline resource is that is not blank node.
     */
    private static PipelineExecutionProfile sanitizeProfile(
            PipelineExecutionProfile profile, Resource pipeline) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Resource resource;
        if (pipeline.isBNode()) {
            resource = valueFactory.createBNode();
        } else {
            resource = valueFactory.createIRI(pipeline + "/profile/default");
        }

        PipelineExecutionProfile result;
        if (profile == null) {
            result = new PipelineExecutionProfile(resource);
        } else {
            result = new PipelineExecutionProfile(
                    resource,
                    profile.rdfRepositoryPolicy(),
                    profile.rdfRepositoryType());
        }
        return result;
    }

}
