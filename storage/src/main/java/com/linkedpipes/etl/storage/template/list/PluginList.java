package com.linkedpipes.etl.storage.template.list;

import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.plugin.loader.PluginJarFile;

import java.util.HashMap;
import java.util.Map;

public class PluginList implements TemplateEventListener {

    private final Map<String, PluginJarFile> plugins = new HashMap<>();

    @Override
    public void onPluginLoaded(PluginJarFile jarFile) {
        plugins.put(jarFile.getJarIri(), jarFile);
    }

    public PluginJarFile getPluginJarFile(String iri) {
        return plugins.get(iri);
    }

}
