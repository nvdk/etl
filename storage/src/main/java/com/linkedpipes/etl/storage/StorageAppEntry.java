package com.linkedpipes.etl.storage;

import com.linkedpipes.etl.storage.cli.CliCommand;
import com.linkedpipes.etl.storage.cli.CliCommandParser;
import com.linkedpipes.etl.storage.cli.StorageConfiguration;
import com.linkedpipes.etl.storage.cli.StorageConfigurationLoader;
import com.linkedpipes.etl.storage.cli.command.CliExportStore;
import com.linkedpipes.etl.storage.cli.command.CliForceDomain;
import com.linkedpipes.etl.storage.cli.command.CliImportStore;
import com.linkedpipes.etl.storage.cli.command.CliStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Arrays;
import java.util.List;

public class StorageAppEntry {

    private static final Logger LOG = LoggerFactory.getLogger(StorageAppEntry.class);

    private static final List<CliCommand> COMMANDS = Arrays.asList(
            new CliImportStore(),
            new CliExportStore(),
            new CliStart(),
            new CliForceDomain()
    );

    private StorageConfiguration configuration = null;

    public static void main(String[] args) {
        redirectJavaLoggingToSl4j();
        System.exit((new StorageAppEntry()).run(args));
    }

    public static void redirectJavaLoggingToSl4j() {
        // https://stackoverflow.com/questions/6020545/send-redirect-route-java-util-logging-logger-jul-to-logback-using-slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public int run(String[] args) {
        try {
            args = loadConfiguration(args);
        } catch (StorageException ex) {
            LOG.error("Can't load configuration.", ex);
            return 1;
        }
        CliCommand command = getCommand(args);
        if (command == null) {
            LOG.error("No command found!");
            return 1;
        }
        LOG.info("Running command: {}",
                command.getCliCommandParser().getCommand());
        int result = executeCommand(command, args);
        LOG.info("Execution finished.");
        return result;
    }

    private String[] loadConfiguration(String[] args) throws StorageException {
        StorageConfigurationLoader reader = new StorageConfigurationLoader();
        String[] result = reader.load(args);
        configuration = reader.getConfiguration();
        return result;
    }

    private CliCommand getCommand(String[] args) {
        if (args.length == 0) {
            return null;
        }
        String commandName = args[0];
        for (CliCommand command : COMMANDS) {
            CliCommandParser parser = command.getCliCommandParser();
            if (parser.getCommand().equals(commandName)) {
                return command;
            }
        }
        return null;
    }

    private int executeCommand(CliCommand command, String[] args) {
        String[] argsWithoutCommand = Arrays.copyOfRange(args, 1, args.length);
        try {
            command.execute(configuration, argsWithoutCommand);
            return 0;
        } catch (Throwable ex) {
            LOG.error("Command execution failed.", ex);
            return 1;
        }
    }

}
