package com.linkedpipes.etl.storage.importer;

import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportOptionsAdapter {

    protected static final String IMPORT_OPTIONS =
            "http://linkedpipes.com/ontology/ImportOptions";

    protected static final String HAS_PIPELINE_OPTIONS =
            "http://etl.linkedpipes.com/ontology/pipelineOptions";

    protected static final String HAS_PIPELINE =
            "http://etl.linkedpipes.com/ontology/pipeline";

    protected static final String HAS_IMPORT_TEMPLATES =
            "http://etl.linkedpipes.com/ontology/importTemplates";

    protected static final String HAS_UPDATE_TEMPLATES =
            "http://etl.linkedpipes.com/ontology/updateTemplates";

    protected static final String HAS_PIPELINE_SUFFIX =
            "http://etl.linkedpipes.com/ontology/targetSuffix";

    protected static final String HAS_PREF_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    protected static final String HAS_KEEP_RESOURCES =
            "http://etl.linkedpipes.com/ontology/keepResources";

    public static List<ImportOptions> asImportOptions(
            StatementsSelector statements) {
        Statements resources = statements.selectSubjectsWithType(
                IMPORT_OPTIONS);
        return resources.stream()
                .map(st -> asImportOptions(
                        statements.selectByGraph(st.getContext()).selector(),
                        st.getSubject()))
                .toList();
    }

    protected static ImportOptions asImportOptions(
            StatementsSelector statements, Resource resource) {
        Map<Resource, ImportOptions.PipelineOptions> pipelines =
                new HashMap<>();
        boolean importTemplates = false;
        boolean updateLocalTemplates = false;
        for (Statement statement : statements.withResource(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case HAS_PIPELINE_OPTIONS:
                    if (value.isResource()) {
                        ImportOptions.PipelineOptions options =
                                loadPipelineOptions(
                                        statements, (Resource) value);
                        pipelines.put(options.pipeline(), options);
                    }
                    break;
                case HAS_IMPORT_TEMPLATES:
                    if (value.isLiteral()) {
                        importTemplates = ((Literal) value).booleanValue();
                    }
                    break;
                case HAS_UPDATE_TEMPLATES:
                    if (value.isLiteral()) {
                        updateLocalTemplates = ((Literal) value).booleanValue();
                    }
                    break;
                default:
                    break;
            }
        }

        return new ImportOptions(
                pipelines, importTemplates, updateLocalTemplates);
    }

    protected static ImportOptions.PipelineOptions loadPipelineOptions(
            StatementsSelector statements, Resource resource) {
        Resource pipeline = null;
        String targetSuffix = null;
        Literal pipelineLabel = null;
        boolean keepResources = false;
        for (Statement statement : statements.withResource(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case HAS_PIPELINE:
                    if (value.isResource()) {
                        pipeline = (Resource) value;
                    }
                    break;
                case HAS_PIPELINE_SUFFIX:
                    if (value.isLiteral()) {
                        targetSuffix = value.stringValue();
                    }
                    break;
                case HAS_PREF_LABEL:
                    if (value instanceof Literal) {
                        pipelineLabel = (Literal) value;
                    }
                    break;
                case HAS_KEEP_RESOURCES:
                    if (value.isLiteral()) {
                        keepResources = ((Literal) value).booleanValue();
                    }
                    break;
                default:
                    break;
            }
        }
        return new ImportOptions.PipelineOptions(
                pipeline, targetSuffix, pipelineLabel, !keepResources);
    }

    public static Statements asRdf(
            ImportOptions options, Resource resource, Resource graph) {
        StatementsBuilder result = Statements.arrayList().builder();
        result.setDefaultGraph(graph);
        result.addIri(resource, RDF.TYPE, IMPORT_OPTIONS);
        if (options.importNewTemplates()) {
            result.addBoolean(resource, HAS_IMPORT_TEMPLATES, true);
        }
        if (options.updateLocalTemplates()) {
            result.addBoolean(resource, HAS_UPDATE_TEMPLATES, true);
        }
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (ImportOptions.PipelineOptions value :
                options.pipelines().values()) {
            Resource node = valueFactory.createBNode();
            result.add(resource, HAS_PIPELINE_OPTIONS, node);
            result.add(node, HAS_PIPELINE, value.pipeline());
            if (value.suffix() != null) {
                result.addString(node, HAS_PIPELINE_SUFFIX, value.suffix());
            }
            if (!value.updateResources()) {
                result.addBoolean(node, HAS_KEEP_RESOURCES, true);
            }
        }
        return result;
    }

}