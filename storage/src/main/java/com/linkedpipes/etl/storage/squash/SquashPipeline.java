package com.linkedpipes.etl.storage.squash;

import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Take a full pipeline with components and squash reference templates
 * to their plugins. Components in squashed pipeline use only PluginTemplates.
 */
public class SquashPipeline {

    private final TemplateApi templateApi;

    public SquashPipeline(TemplateApi templateApi) {
        this.templateApi = templateApi;
    }

    public Pipeline squash(Pipeline pipeline) throws StorageException {
        List<PipelineComponent> components = new ArrayList<>();
        for (PipelineComponent component : pipeline.components()) {
            List<String> ancestors = templateApi.getAncestors(
                    component.template().stringValue());
            Optional<PluginTemplate> pluginOptional =
                    templateApi.loadPluginTemplate(ancestors.get(0));
            if (pluginOptional.isEmpty()) {
                components.add(component);
                continue;
            }
            PluginTemplate plugin = pluginOptional.get();
            Statements parentConfiguration =
                    templateApi.getEffectiveConfiguration(
                            component.template().stringValue())
                            .orElseGet(Statements::empty);
            List<Statement> configuration;
            try {
                configuration = ConfigurationFacade.merge(
                        List.of(
                                parentConfiguration.asList(),
                                component.configuration().asList()),
                        plugin.configurationDescription(),
                        component.configurationGraph().stringValue(),
                        component.configurationGraph());
            } catch (InvalidConfiguration ex) {
                throw new StorageException(
                        "Can't prepare configuration for: {}",
                        component.resource(), ex);
            }
            components.add(new PipelineComponent(
                    component.resource(), component.label(),
                    component.description(), component.note(),
                    component.color(), component.xPosition(),
                    component.yPosition(),
                    plugin.resource(),
                    component.disabled(), component.configurationGraph(),
                    Statements.wrap(configuration)));
        }
        return new Pipeline(
                pipeline.resource(), pipeline.label(),
                pipeline.version(), pipeline.note(), pipeline.tags(),
                pipeline.executionProfile(),
                components, pipeline.connections());
    }

}
