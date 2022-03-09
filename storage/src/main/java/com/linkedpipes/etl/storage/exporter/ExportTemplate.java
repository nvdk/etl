package com.linkedpipes.etl.storage.exporter;

import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ExportTemplate {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExportTemplate.class);

    private final TemplateApi templatesApi;

    public ExportTemplate(TemplateApi templatesApi) {
        this.templatesApi = templatesApi;
    }

    public Optional<ReferenceTemplate> exportTemplate(
            String iri, ExportOptions options)
            throws StorageException {
        if (templatesApi.isPluginTemplate(iri)) {
            return Optional.empty();
        }
        Optional<ReferenceTemplate> templateWrap =
                templatesApi.loadReferenceTemplate(iri);
        if (templateWrap.isEmpty()) {
            return Optional.empty();
        }
        ReferenceTemplate template = templateWrap.get();
        if (options.removePrivateConfiguration()) {
            template = new ReferenceTemplate(
                    template.resource(), template.template(),
                    template.prefLabel(), template.description(),
                    template.note(), template.color(), template.tags(),
                    template.knownAs(), template.pluginTemplate(),
                    template.version(),
                    removePrivateConfiguration(
                            template.configuration(), template.template()),
                    template.configurationGraph());
        }
        return Optional.of(template);
    }

    public Statements removePrivateConfiguration(
            Statements statements, Resource template)
            throws StorageException {
        String pluginIri = getPluginTemplate(template);
        Optional<PluginTemplate> pluginTemplate =
                templatesApi.loadPluginTemplate(pluginIri);
        if (pluginTemplate.isEmpty()) {
            LOG.warn("Can't find plugin template for: {}", template);
            return statements;
        }
        return Statements.wrap(ConfigurationFacade.removePrivateStatements(
                statements, pluginTemplate.get().configurationDescription()));
    }

    private String getPluginTemplate(Resource template)
            throws StorageException {
        return templatesApi.getAncestors(template.stringValue()).get(0);
    }

}
