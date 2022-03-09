package com.linkedpipes.etl.storage.assistant;

import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class AssistantDataAdapter {

    public static final String PIPELINE_INFO =
            "https://demo.etl.linkedpipes.com/resources/pipelines/info";

    public static Statements asRdf(AssistantData data) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        StatementsBuilder result = Statements.arrayList().builder();
        IRI resource = valueFactory.createIRI(PIPELINE_INFO);
        result.setDefaultGraph(resource);
        result.addIri(resource, RDF.TYPE, LP.PIPELINE_INFORMATION);
        for (var entry : data.tags().entrySet()) {
            result.add(resource, LP.HAS_TAG, entry.getKey());
        }
        int counter = 0;
        for (var entry : data.followup().entrySet()) {
            IRI iri = valueFactory.createIRI(
                    resource.getNamespace() + "followup/" + ++counter);
            result.add(resource, LP.HAS_FREQUENCY, iri);

            AssistantData.ComponentPair pait = entry.getKey();
            result.add(iri, LP.HAS_SOURCE_COMPONENT, pait.source());
            result.add(iri, LP.HAS_TARGET_COMPONENT, pait.target());
            result.addInt(iri, LP.HAS_FREQUENCY, entry.getValue());
        }
        return result;
    }

    public static Statements asRdf(PipelineIriList data) {
        StatementsBuilder result = Statements.arrayList().builder();
        for (String iri : data) {
            result.addIri(iri, RDF.TYPE, LP.PIPELINE);
        }
        return result;
    }

}
