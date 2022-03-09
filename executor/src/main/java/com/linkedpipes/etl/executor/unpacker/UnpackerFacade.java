package com.linkedpipes.etl.executor.unpacker;

/**
 * Provide capabilities to unpack pipeline for execution.
 */
public class UnpackerFacade {

//    @Autowired
//    private PipelineFacade pipelines;
//
//    @Autowired
//    private TemplateFacade templates;
//
//    @Autowired
//    private ExecutionFacade executions;
//
//    public Collection<Statement> unpack(
//            Pipeline pipeline, Collection<Statement> configurationRdf)
//            throws StorageException {
//        return unpack(pipelines.getPipelineRdf(pipeline), configurationRdf);
//    }
//
//    public Collection<Statement> unpack(
//            Collection<Statement> pipelineRdf,
//            Collection<Statement> configurationRdf) throws StorageException {
//        UnpackOptions options = new UnpackOptions();
//        ClosableRdf4jSource optionsSource = Rdf4jSource.wrapInMemory(
//                configurationRdf);
//        try {
//            RdfUtils.loadByType(optionsSource, null, options,
//                    UnpackOptions.TYPE);
//        } catch (InvalidNumberOfResults ex) {
//            // Ignore as the option is optional.
//        } catch (RdfUtilsException ex) {
//            throw new StorageException("Can't load execution options.", ex);
//        } finally {
//            optionsSource.close();
//        }
//
//        ClosableRdf4jSource source = Rdf4jSource.wrapInMemory(pipelineRdf);
//        DesignerPipeline pipeline;
//        GraphCollection graphs;
//        try {
//            pipeline = ModelLoader.loadDesignerPipeline(source);
//            graphs = ModelLoader.loadConfigurationGraphs(source, pipeline);
//        } catch (RdfUtilsException ex) {
//            throw new StorageException("Can't unpack pipeline.", ex);
//        } finally {
//            source.close();
//        }
//
//        DesignerToExecutor designerToExecutor =
//                new DesignerToExecutor(templates, executions);
//        designerToExecutor.transform(pipeline, graphs, options);
//
//        ExecutorPipeline executorPipeline = designerToExecutor.getTarget();
//
//        StatementsCollector collector = new StatementsCollector(
//                executorPipeline.getIri());
//        executorPipeline.write(collector);
//        for (String graph : executorPipeline.getReferencedGraphs()) {
//            graphs.get(graph).forEach((statement -> {
//                collector.add(statement);
//            }));
//        }
//        return collector.getStatements();
//    }

}
