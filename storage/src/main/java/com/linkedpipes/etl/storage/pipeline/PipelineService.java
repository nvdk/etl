package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.StorageVersion;
import com.linkedpipes.etl.storage.importer.AlignPipelineResources;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineList;
import com.linkedpipes.etl.storage.pipeline.model.PipelineListItem;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PipelineService implements PipelineApi {

    private final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    private final PipelineStore store;

    public PipelineService(PipelineStore store) {
        this.store = store;
    }

    @Override
    public String reservePipelineIri(String suffix) throws StorageException {
        return store.reservePipelineIri(suffix);
    }

    @Override
    public Collection<String> listPipelines() throws StorageException {
        return store.listPipelines();
    }

    @Override
    public Optional<Pipeline> loadPipeline(String iri) throws StorageException {
        return store.loadPipeline(iri);
    }

    @Override
    public Pipeline storePipeline(Pipeline pipeline) throws StorageException {
        if (!StorageVersion.isCurrent(pipeline.version())) {
            throw new StorageException(
                    "Invalid version: {}", pipeline.version());
        }
        if (pipeline.resource() == null || pipeline.resource().isBNode()) {
            return storeNewPipeline(pipeline);
        } else {
            store.storePipeline(pipeline);
            return pipeline;
        }
    }

    private Pipeline storeNewPipeline(Pipeline pipeline)
            throws StorageException {
        Resource resource = valueFactory.createIRI(
                store.reservePipelineIri(null));
        Pipeline result = AlignPipelineResources.apply(pipeline, resource);
        store.storePipeline(result);
        return result;
    }

    @Override
    public void deletePipeline(String iri) throws StorageException {
        store.deletePipeline(iri);
    }

    @Override
    public PipelineList createPipelineList() throws StorageException {
        // TODO We can consider caching here.
        List<PipelineListItem> pipelines = new ArrayList<>();
        for (String iri : listPipelines()) {
            Optional<Pipeline> pipelineWrap = loadPipeline(iri);
            if (pipelineWrap.isEmpty()) {
                continue;
            }
            Pipeline pipeline = pipelineWrap.get();
            pipelines.add(new PipelineListItem(
                    pipeline.resource(),
                    pipeline.label(),
                    pipeline.tags()));
        }
        return new PipelineList(pipelines);
    }

}
