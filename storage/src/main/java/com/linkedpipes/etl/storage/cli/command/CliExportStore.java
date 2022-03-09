package com.linkedpipes.etl.storage.cli.command;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.cli.CliCommand;
import com.linkedpipes.etl.storage.cli.CliCommandParser;
import com.linkedpipes.etl.storage.cli.ComponentManager;
import com.linkedpipes.etl.storage.cli.StorageConfiguration;
import com.linkedpipes.etl.storage.exporter.ExportContent;
import com.linkedpipes.etl.storage.exporter.ExportContentData;
import com.linkedpipes.etl.storage.exporter.ExportOptions;
import com.linkedpipes.etl.storage.importer.ImportOptions;
import com.linkedpipes.etl.storage.importer.ImportOptionsAdapter;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.pipeline.adapter.PipelineAdapter;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsFile;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CliExportStore implements CliCommand {

    private record Arguments(
            boolean removePrivateConfiguration,
            boolean force,
            String outputPath
    ) {

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(CliExportStore.class);

    @Override
    public CliCommandParser getCliCommandParser() {
        Options options = new Options();
        options.addOption(
                null, "remove-private-configuration", false,
                "If set to true remove private configurations.");
        options.addOption(
                null, "output", true,
                "Path to output file.");
        options.addOption(
                null, "force", false,
                "Ignore errors in output.");
        return new CliCommandParser(
                "export-store",
                "Export all pipelines and templates into a single file.",
                options);
    }

    @Override
    public int execute(StorageConfiguration configuration, String[] args) {
        ComponentManager components = new ComponentManager(configuration);
        try {
            components.initialize();
        } catch (StorageException ex) {
            LOG.error("Can't initialize application.", ex);
            return 1;
        }
        int result = innerExecute(components, args);
        components.shutdown();
        return result;
    }

    private int innerExecute(ComponentManager components, String[] args) {
        Arguments arguments = loadArguments(args);
        if (arguments == null) {
            return 1;
        }
        List<Pipeline> pipelines;
        LOG.info("Collecting pipelines.");
        try {
            pipelines = loadPipelines(components.pipelineApi());
        } catch (StorageException ex) {
            LOG.error("Can't list all pipelines.");
            return 1;
        }
        LOG.info("Preparing {} pipelines for export.", pipelines.size());
        ExportContent worker = new ExportContent(
                components.templateApi(),
                new ExportOptions(
                        arguments.removePrivateConfiguration,
                        arguments.force));
        ExportContentData exportData;
        try {
            exportData = worker.exportPipelines(pipelines);
        } catch (StorageException ex) {
            LOG.error("Can't prepare data for export.", ex);
            return 1;
        }
        LOG.info("Preparing import options.");
        ImportOptions options = createImportOptions(exportData.pipelines());
        LOG.info("Writing data to output file.");
        try {
            saveData(exportData, options, new File(arguments.outputPath));
        } catch (IOException ex) {
            LOG.error("Can't write data to file.", ex);
            return 1;
        }
        return 0;
    }

    private Arguments loadArguments(String[] args) {
        CliCommandParser parser = getCliCommandParser();
        CommandLine cmd = parser.parse(args);
        if (cmd == null) {
            return null;
        }
        return new Arguments(
                cmd.hasOption("remove-private-configuration"),
                cmd.hasOption("force"),
                cmd.getOptionValue("output"));
    }

    private List<Pipeline> loadPipelines(PipelineApi pipelineApi)
            throws StorageException {
        List<Pipeline> result = new ArrayList<>();
        for (String iri : pipelineApi.listPipelines()) {
            Optional<Pipeline> pipelineOptional = pipelineApi.loadPipeline(iri);
            if (pipelineOptional.isEmpty()) {
                continue;
            }
            result.add(pipelineOptional.get());
        }
        return result;
    }

    private ImportOptions createImportOptions(Collection<Pipeline> pipelines) {
        Map<Resource, ImportOptions.PipelineOptions> options = new HashMap<>();
        for (Pipeline pipeline : pipelines) {
            String iri = pipeline.resource().stringValue();
            String suffix = iri.substring(iri.lastIndexOf("/pipelines/") + 11);
            options.put(pipeline.resource(),
                    new ImportOptions.PipelineOptions(
                            pipeline.resource(), suffix, null, true));
        }
        return new ImportOptions(options, false, false);
    }

    private void saveData(
            ExportContentData data, ImportOptions options, File file)
            throws IOException {
        StatementsFile statements = Statements.arrayList().file();
        for (Pipeline pipeline : data.pipelines()) {
            statements.addAll(PipelineAdapter.asRdf(pipeline));
        }
        for (ReferenceTemplate template : data.referenceTemplates()) {
            statements.addAll(ReferenceTemplateAdapter.asRdf(template));
        }
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        statements.addAll(ImportOptionsAdapter.asRdf(
                options, valueFactory.createBNode(), null));
        statements.writeToFile(file, RDFFormat.TRIG);
    }

}
