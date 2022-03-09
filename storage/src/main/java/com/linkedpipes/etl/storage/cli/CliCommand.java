package com.linkedpipes.etl.storage.cli;

public interface CliCommand {

    CliCommandParser getCliCommandParser();

    int execute(StorageConfiguration configuration, String[] args);

}
