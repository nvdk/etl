package com.linkedpipes.etl.storage.exporter;

public record ExportOptions(
        /*
         * If true remove private configuration statements.
         */
        boolean removePrivateConfiguration,
        /*
         * It true ignore missing templates.
         */
        boolean ignoreMissingTemplates
) {

}
