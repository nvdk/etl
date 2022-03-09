package com.linkedpipes.etl.storage.template.plugin.adapter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.plugin.adapter.rdf.RdfToPluginTemplate;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.plugin.loader.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginToPluginTemplate {

    public PluginTemplate asPluginTemplate(Plugin plugin)
            throws StorageException {
        PluginTemplate result = RdfToPluginTemplate.asPluginTemplate(
                Statements.wrap(plugin.definition).selector(),
                Statements.wrap(plugin.configuration),
                Statements.wrap(plugin.configurationDescription));
        // As dialogs are not part of the information in the template,
        // we need to create them manually.
        return new PluginTemplate(
                result.resource(),
                result.prefLabel(),
                result.color(),
                result.type(),
                result.supportControl(),
                result.tags(),
                result.infoLink(),
                createDialogs(plugin),
                result.ports(),
                result.jarResource(),
                result.requirement(),
                result.configuration(),
                result.configurationGraph(),
                result.configurationDescription(),
                result.configurationDescriptionGraph());
    }

    private List<String> createDialogs(Plugin plugin) {
        Set<String> files = plugin.fileEntries.keySet();
        Set<String> candidates = new HashSet<>();
        // Find all with prefix dialog/
        for (String file : files) {
            if (!file.startsWith("dialog/")) {
                continue;
            }
            String name = file.substring(
                    file.indexOf("/") + 1,
                    file.lastIndexOf("/"));
            candidates.add(name);
        }
        return new ArrayList<>(candidates);
    }

}
