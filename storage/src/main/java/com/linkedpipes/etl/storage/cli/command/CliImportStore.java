package com.linkedpipes.etl.storage.cli.command;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.cli.CliCommand;
import com.linkedpipes.etl.storage.cli.CliCommandParser;
import com.linkedpipes.etl.storage.cli.ComponentManager;
import com.linkedpipes.etl.storage.cli.StorageConfiguration;
import com.linkedpipes.etl.storage.importer.ImportContent;
import com.linkedpipes.etl.storage.importer.ImportOptions;
import com.linkedpipes.etl.storage.importer.ImportOptionsAdapter;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsFile;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CliImportStore implements CliCommand {

    private record Arguments(
            String inputPath
    ) {

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(CliImportStore.class);

    @Override
    public CliCommandParser getCliCommandParser() {
        Options options = new Options();
        options.addOption(
                null, "input", true,
                "Path to input file.");
        return new CliCommandParser(
                "import-store",
                "Import all pipelines and templates from given file.",
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
        LOG.info("Loading input file.");
        StatementsFile contentRdf = Statements.arrayList().file();
        try {
            contentRdf.addAllIfExists(new File(arguments.inputPath));
        } catch (IOException ex) {
            LOG.error("Can't read input data file.", ex);
            return 1;
        }
        LOG.info("Loading import options from input.");
        ImportOptions importOption;
        List<ImportOptions> importOptions =
                ImportOptionsAdapter.asImportOptions(contentRdf.selector());
        if (importOptions.size() > 1) {
            LOG.error("At most one option object can be provide. Found {}",
                    importOptions.size());
            return 1;
        } else if (importOptions.isEmpty()) {
            importOption = ImportOptions.defaultOptions();
        } else {
            importOption = importOptions.get(0);
        }
        LOG.info("Importing content.");
        ImportContent worker = new ImportContent(
                components.pipelineApi(), components.templateApi());
        try {
            worker.importStatements(contentRdf, new ImportOptions(
                    importOption.pipelines(),
                    true, false));
        } catch (StorageException ex) {
            LOG.error("Can't import data.", ex);
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
        return new Arguments(cmd.getOptionValue("input"));
    }

}
