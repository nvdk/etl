package com.linkedpipes.etl.storage.pipeline.migration;

import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PipelineV0 {

    private static final List<String> TEMPLATES;

    static {
        String prefix = "http://etl.linkedpipes.com/resources/components/";
        TEMPLATES = Arrays.asList(
                prefix + "c-files/0.0.0",
                prefix + "c-rdfChunkedTriples/0.0.0",
                prefix + "c-rdfSingleGraph/0.0.0",
                prefix + "e-datasetMetadata/0.0.0",
                prefix + "e-dcatAp11Dataset/0.0.0",
                prefix + "e-dcatAp11Distribution/0.0.0",
                prefix + "e-distributionMetadata/0.0.0",
                prefix + "e-filesFromLocal/0.0.0",
                prefix + "e-ftpFiles/0.0.0",
                prefix + "e-httpGetFile/0.0.0",
                prefix + "e-httpGetFiles/0.0.0",
                prefix + "e-pipelineInput/0.0.0",
                prefix + "e-sparqlEndpoint/0.0.0",
                prefix + "e-sparqlEndpointChunked/0.0.0",
                prefix + "e-sparqlEndpointChunkedList/0.0.0",
                prefix + "e-sparqlEndpointConstructScrollableCursor/0.0.0",
                prefix + "e-sparqlEndpointList/0.0.0",
                prefix + "e-sparqlEndpointSelect/0.0.0",
                prefix + "e-sparqlEndpointSelectScrollableCursor/0.0.0",
                prefix + "e-textHolder/0.0.0",
                prefix + "e-voidDataset/0.0.0",
                prefix + "l-couchDb/0.0.0",
                prefix + "l-dcatAp11ToCkan/0.0.0",
                prefix + "l-dcatApToCkan/0.0.0",
                prefix + "l-filesToLocal/0.0.0",
                prefix + "l-filesToScp/0.0.0",
                prefix + "l-ftpFiles/0.0.0",
                prefix + "l-graphStoreProtocol/0.0.0",
                prefix + "l-lodCloud/0.0.0",
                prefix + "l-solr/0.0.0",
                prefix + "l-sparqlEndpoint/0.0.0",
                prefix + "l-sparqlEndpointChunked/0.0.0",
                prefix + "l-wikibase/0.0.0",
                prefix + "q-sparqlAsk/0.0.0",
                prefix + "t-bingTranslator/0.0.0",
                prefix + "t-chunkedToFiles/0.0.0",
                prefix + "t-chunkedToGraph/0.0.0",
                prefix + "t-chunkedToTurtle/0.0.0",
                prefix + "t-chunkSplitter/0.0.0",
                prefix + "t-excelToCsv/0.0.0",
                prefix + "t-fileHasher/0.0.0",
                prefix + "t-filesBase64Decode/0.0.0",
                prefix + "t-filesFilter/0.0.0",
                prefix + "t-filesRenamer/0.0.0",
                prefix + "t-filesToRdf/0.0.0",
                prefix + "t-filesToRdfChunked/0.0.0",
                prefix + "t-filesToRdfGraph/0.0.0",
                prefix + "t-filesToStatements/0.0.0",
                prefix + "t-geoTools/0.0.0",
                prefix + "t-graphMerger/0.0.0",
                prefix + "t-hdtToRdf/0.0.0",
                prefix + "t-htmlCssUv/0.0.0",
                prefix + "t-jsonLdFormat/0.0.0",
                prefix + "t-jsonLdFormatTitanium/0.0.0",
                prefix + "t-jsonLdToRdf/0.0.0",
                prefix + "t-jsonLdToRdfChunked/0.0.0",
                prefix + "t-jsonLdToRdfTitanium/0.0.0",
                prefix + "t-jsonToJsonLd/0.0.0",
                prefix + "t-modifyDate/0.0.0",
                prefix + "t-mustache/0.0.0",
                prefix + "t-mustacheChunked/0.0.0",
                prefix + "t-packZip/0.0.0",
                prefix + "t-propertyLinkerChunked/0.0.0",
                prefix + "t-rdfDifference/0.0.0",
                prefix + "t-rdfToFile/0.0.0",
                prefix + "t-rdfToFileChunked/0.0.0",
                prefix + "t-rdfToHdt/0.0.0",
                prefix + "t-rdfToJsonTemplate/0.0.0",
                prefix + "t-rdfToWrappedJsonLdChunked/0.0.0",
                prefix + "t-shacl/0.0.0",
                prefix + "t-shaclJena/0.0.0",
                prefix + "t-singleGraphUnion/0.0.0",
                prefix + "t-sparqlConstruct/0.0.0",
                prefix + "t-sparqlConstructChunked/0.0.0",
                prefix + "t-sparqlConstructToFileList/0.0.0",
                prefix + "t-sparqlLinkerChunked/0.0.0",
                prefix + "t-sparqlPerGraphUpdate/0.0.0",
                prefix + "t-sparqlSelect/0.0.0",
                prefix + "t-sparqlSelectMulti/0.0.0",
                prefix + "t-sparqlUpdate/0.0.0",
                prefix + "t-sparqlUpdateChunked/0.0.0",
                prefix + "t-streamCompression/0.0.0",
                prefix + "t-tabular/0.0.0",
                prefix + "t-tabularChunked/0.0.0",
                prefix + "t-tabularUv/0.0.0",
                prefix + "t-templatedXlsToCsv/0.0.0",
                prefix + "t-unpack/0.0.0",
                prefix + "t-unpackZip/0.0.0",
                prefix + "t-valueParser/0.0.0",
                prefix + "t-xmlToChunks/0.0.0",
                prefix + "t-xslt/0.0.0",
                prefix + "x-deleteDirectory/0.0.0",
                prefix + "x-graphStorePurger/0.0.0",
                prefix + "x-httpRequest/0.0.0",
                prefix + "x-virtuoso/0.0.0",
                prefix + "x-virtuosoExtractor/0.0.0"
        );
    }

    /**
     * Change core templates from local host to etl.linkedpipes.com.
     *
     * <p>Example of conversion:
     * http://localhost:8080/resources/components/t-tabular
     * http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0
     */
    public Pipeline migrateToV1(Pipeline pipeline) {
        List<PipelineComponent> components = pipeline.components().stream()
                .map(this::migrateComponent)
                .collect(Collectors.toList());
        return new Pipeline(
                pipeline.resource(),
                pipeline.label(),
                pipeline.version(),
                pipeline.note(),
                pipeline.tags(),
                pipeline.executionProfile(),
                components,
                pipeline.connections()
        );
    }

    private PipelineComponent migrateComponent(PipelineComponent component) {
        String name = templateName(component.template().stringValue());
        IRI newTemplate = searchMatchingTemplateByName(name);
        if (newTemplate == null) {
            return component;
        }
        return new PipelineComponent(
                component.resource(),
                component.label(),
                component.description(),
                component.note(),
                component.color(),
                component.xPosition(),
                component.yPosition(),
                newTemplate,
                component.disabled(),
                component.configurationGraph(),
                component.configuration()
        );
    }

    private String templateName(String iri) {
        // The extracted name is /t-tabular and we add / to the end
        // to prevent t-tabular to match t-tabularUv, also every name
        // is followed by /{version}.
        return iri.substring(iri.lastIndexOf("/")) + "/";
    }

    private IRI searchMatchingTemplateByName(String name) {
        for (String iri : TEMPLATES) {
            if (iri.contains(name)) {
                return SimpleValueFactory.getInstance().createIRI(iri);
            }
        }
        return null;
    }

}
