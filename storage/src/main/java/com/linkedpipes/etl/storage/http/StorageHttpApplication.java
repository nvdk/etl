package com.linkedpipes.etl.storage.http;

import com.linkedpipes.etl.storage.assistant.AssistantApi;
import com.linkedpipes.etl.storage.http.servlet.AssistantServlet;
import com.linkedpipes.etl.storage.http.servlet.AssistantServletService;
import com.linkedpipes.etl.storage.http.servlet.TemplateServletService;
import com.linkedpipes.etl.storage.http.servlet.TemplateServlet;
import com.linkedpipes.etl.storage.http.servlet.PipelineServlet;
import com.linkedpipes.etl.storage.http.servlet.PipelineServletService;
import com.linkedpipes.etl.storage.pipeline.PipelineApi;
import com.linkedpipes.etl.storage.plugin.PluginApi;
import com.linkedpipes.etl.storage.template.TemplateApi;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class StorageHttpApplication extends ResourceConfig {

    protected TemplateServletService componentServletService;

    protected PipelineServletService pipelineServletService;

    protected AssistantServletService assistantServletService;

    public StorageHttpApplication(
            PluginApi pluginApi,
            TemplateApi templateApi,
            PipelineApi pipelineApi,
            AssistantApi assistantApi) {
        super();
        registerEndpoints();
        registerFeatures();

        // property(ServletProperties.FILTER_FORWARD_ON_404, false);

        componentServletService = new TemplateServletService(
                pluginApi, templateApi);

        pipelineServletService = new PipelineServletService(
                pipelineApi, templateApi);

        assistantServletService = new AssistantServletService(
                assistantApi);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(componentServletService);
                bind(pipelineServletService);
                bind(assistantServletService);
            }
        });
    }

    private void registerEndpoints() {
        register(PipelineServlet.class);
        register(TemplateServlet.class);
        register(AssistantServlet.class);
    }

    private void registerFeatures() {
        register(MultiPartFeature.class);
//        register(RestJsonProvider.class)
//        register(JacksonFeature.class)
    }

    public void onShutdown() {
        // TODO
    }

}
