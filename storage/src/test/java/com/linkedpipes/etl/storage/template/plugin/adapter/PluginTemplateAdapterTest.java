package com.linkedpipes.etl.storage.template.plugin.adapter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.plugin.loader.Plugin;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.jar.JarEntry;

public class PluginTemplateAdapterTest {

    @Test
    public void textHolder() throws StorageException {
        var statements = TestUtils.statements(
                "template/plugin/text-holder.trig").selector();

        var fileEntries = new HashMap<String, JarEntry>();
        fileEntries.put("dialog/config/dialog.js", null);
        fileEntries.put("dialog/config/dialog.html", null);
        fileEntries.put("dialog/template/dialog.js", null);
        fileEntries.put("dialog/template/dialog.html", null);
        fileEntries.put("dialog/instance/dialog.js", null);
        fileEntries.put("dialog/instance/dialog.html", null);

        var input = new Plugin(
                null,
                null,
                null,
                null,
                statements.selectByGraph("http://definition")
                        .asList(),
                statements.selectByGraph("http://configuration")
                        .asList(),
                statements.selectByGraph("http://configuration-description")
                        .asList(),
                fileEntries
        );

        var plugin = PluginTemplateAdapter.asPluginTemplate(input);

        var definitionRdf = PluginTemplateAdapter.definitionAsRdf(plugin);
        TestUtils.assertIsomorphicIgnoreGraph(
                statements.selectByGraph(
                        "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0"),
                definitionRdf);

        var configurationRdf =
                PluginTemplateAdapter.configurationAsRdf(plugin);
        TestUtils.assertIsomorphicIgnoreGraph(
                statements.selectByGraph(
                        "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0/configuration"),
                configurationRdf);

        var configurationDescRdf =
                PluginTemplateAdapter.configurationDescriptionAsRdf(plugin);
        TestUtils.assertIsomorphicIgnoreGraph(
                statements.selectByGraph(
                        "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0/configuration-description"),
                configurationDescRdf);

    }

}
