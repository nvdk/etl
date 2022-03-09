package com.linkedpipes.etl.storage.template.plugin.adapter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.template.plugin.adapter.rdf.PluginTemplateToRdf;
import com.linkedpipes.etl.storage.template.plugin.adapter.rdf.RdfToPluginTemplate;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.plugin.loader.Plugin;

import java.util.List;

public class PluginTemplateAdapter {

    public static List<PluginTemplate> asPluginTemplates(
            Statements statements) {
        return RdfToPluginTemplate.asPluginTemplates(statements.selector());
    }

    public static Statements definitionAsRdf(PluginTemplate definition) {
        return (new PluginTemplateToRdf()).definitionAsRdf(definition);
    }

    public static Statements configurationAsRdf(PluginTemplate definition) {
        return (new PluginTemplateToRdf()).configurationAsRdf(definition);
    }

    public static Statements configurationDescriptionAsRdf(
            PluginTemplate definition) {
        return (new PluginTemplateToRdf())
                .configurationDescriptionAsRdf(definition);
    }

    public static PluginTemplate asPluginTemplate(Plugin plugin)
            throws StorageException {
        return (new PluginToPluginTemplate()).asPluginTemplate(plugin);
    }

    public static Statements asStatements(PluginTemplate definition) {
        Statements result = Statements.arrayList();
        result.addAll(definitionAsRdf(definition));
        result.addAll(configurationAsRdf(definition));
        result.addAll(configurationDescriptionAsRdf(definition));
        return result;
    }

}
