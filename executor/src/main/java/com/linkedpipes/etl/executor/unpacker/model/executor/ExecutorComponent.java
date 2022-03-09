package com.linkedpipes.etl.executor.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

import java.util.LinkedList;
import java.util.List;

public class ExecutorComponent {

    private String iri;

    // TODO Remove and load from pipeline where needed.
    private String label;

    private List<String> types = new LinkedList<>();

    private List<ExecutorPort> ports = new LinkedList<>();

    private List<String> requirements = new LinkedList<>();

    private String jar;

    private String configGraph;

    private String configDescriptionGraph;

    private String executionType;

    private Integer executionOrder = -1;

    /**
     * Execution that is component is mapped from.
     */
    private String execution;

    public void write(BackendTripleWriter writer) {
        writeSharedInfo(writer);
        switch (executionType) {
            case LP_EXEC.TYPE_EXECUTE:
                writeExecute(writer);
                break;
            case LP_EXEC.TYPE_MAPPED:
                writeMapped(writer);
                break;
            case LP_EXEC.TYPE_SKIP:
                // There are no additional information required.
                break;
            default:
                break;
        }
    }

    private void writeSharedInfo(BackendTripleWriter writer) {
        for (String type : types) {
            writer.iri(iri, RDF.TYPE, type);
        }
        writer.iri(iri, RDF.TYPE, LP_PIPELINE.COMPONENT);
        writer.typed(iri, LP_EXEC.HAS_ORDER_EXEC, executionOrder.toString(),
                XSD.INTEGER);
        writer.iri(iri, LP_PIPELINE.HAS_JAR_URL, jar);
        writer.iri(iri, LP_EXEC.HAS_EXECUTION_TYPE, executionType);
        writer.string(iri, SKOS.PREF_LABEL, label, null);
    }

    private void writeExecute(BackendTripleWriter writer) {
        for (String requirement : requirements) {
            writer.iri(iri, LP_PIPELINE.HAS_REQUIREMENT, requirement);
        }
        writer.iri(iri, LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                configGraph);
        writer.iri(iri, LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                configDescriptionGraph);
        for (ExecutorPort port : ports) {
            writer.iri(iri, LP_PIPELINE.HAS_DATA_UNIT, port.getIri());
            port.write(writer);
        }
        if (execution != null) {
            writer.iri(iri, LP_EXEC.HAS_EXECUTION, execution);
        }
    }

    private void writeMapped(BackendTripleWriter writer) {
        writer.iri(iri, LP_EXEC.HAS_EXECUTION, execution);
        for (ExecutorPort port : ports) {
            writer.iri(iri, LP_PIPELINE.HAS_DATA_UNIT, port.getIri());
            port.write(writer);
        }
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void addPort(ExecutorPort port) {
        ports.add(port);
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getConfigGraph() {
        return configGraph;
    }

    public void setConfigGraph(String configGraph) {
        this.configGraph = configGraph;
    }

    public String getConfigDescriptionGraph() {
        return configDescriptionGraph;
    }

    public void setConfigDescriptionGraph(String configDescriptionGraph) {
        this.configDescriptionGraph = configDescriptionGraph;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public List<ExecutorPort> getPorts() {
        return ports;
    }

    public String getExecutionType() {
        return executionType;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

}
