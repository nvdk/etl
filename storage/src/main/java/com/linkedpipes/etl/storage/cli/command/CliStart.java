package com.linkedpipes.etl.storage.cli.command;

import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.cli.ComponentManager;
import com.linkedpipes.etl.storage.cli.StorageConfiguration;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.cli.CliCommand;
import com.linkedpipes.etl.storage.cli.CliCommandParser;
import com.linkedpipes.etl.storage.http.StorageHttpApplication;
import com.linkedpipes.etl.storage.http.server.JettyHttpServer;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.plugin.PluginApi;
import com.linkedpipes.etl.storage.template.TemplateApi;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliStart implements CliCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CliStart.class);

    private static final int HTTP_SHUTDOWN_TIME_MS = 2000;

    private static final int SERVICE_SHUTDOWN_TIME_MS = 3000;

    @Override
    public CliCommandParser getCliCommandParser() {
        Options options = new Options();
        return new CliCommandParser(
                "start",
                "Start storage.",
                options
        );
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
        int result = innerExecute(configuration, components, args);
        components.shutdown();
        return result;
    }

    private int innerExecute(
            StorageConfiguration configuration,
            ComponentManager components, String[] args) {
        AssistantService assistantService = new AssistantService();
        try {
            assistantService.initialize(components.pipelineApi());
        } catch (StorageException ex) {
            LOG.error("Can't initialize designer assistant.", ex);
            return 1;
        }
        PluginApi pluginApi = components.pluginApi();
        PipelineApi pipelineApi = assistantService.wrapPipelineApi(
                components.pipelineApi());
        TemplateApi templateApi = components.templateApi();
        StorageHttpApplication application =
                new StorageHttpApplication(
                        pluginApi, templateApi, pipelineApi,
                        assistantService);
        JettyHttpServer server = new JettyHttpServer(application);
        try {
            server.startServer(configuration.port());
        } catch (StorageException ex) {
            LOG.error("Can't start HTTP server");
            return 1;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down HTTP services");
            application.onShutdown();
            try {
                Thread.sleep(HTTP_SHUTDOWN_TIME_MS);
            } catch (InterruptedException ex) {
                // Ignore exception.
            }
            LOG.info("Shutting down services");
            components.shutdown();
            try {
                Thread.sleep(SERVICE_SHUTDOWN_TIME_MS);
            } catch (InterruptedException ex) {
                // Ignore exception.
            }
            try {
                LOG.info("Shutting down HTTP server");
                server.stop();
            } catch (Exception ex) {
                LOG.error("Error while shutting down.", ex);
            }
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            LOG.error("Exception while waiting for all to finish.");
        }

        return 0;
    }

}
