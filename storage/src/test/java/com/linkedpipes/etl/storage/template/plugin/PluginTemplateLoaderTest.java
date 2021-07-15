package com.linkedpipes.etl.storage.template.plugin;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.utils.Statements;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginTemplateLoaderTest {

    public static class PluginTemplateLoaderMock extends PluginTemplateLoader {

        @Override
        protected byte[] readJarEntry(JarFile jar, JarEntry entry) {
            return new byte[0];
        }
    }

    @Test
    public void textHolder000() throws Exception {

        String directory = "template/plugin/e-textholder-0.0.0/";
        var configuration = TestUtils.rdfFromResource(
                directory + "template/config.jsonld");
        var description = TestUtils.rdfFromResource(
                directory + "template/config-desc.ttl");
        var definition = TestUtils.rdfFromResource(
                directory + "template/definition.jsonld");

        Map<String, JarEntry> dialogs = new HashMap<>();
        dialogs.put("template/dialog.html", null);
        dialogs.put("template/dialog.js", null);
        dialogs.put("instance/dialog.html", null);
        dialogs.put("instance/dialog.js", null);
        dialogs.put("config/dialog.html", null);
        dialogs.put("config/dialog.js", null);

        String jarIri = "http://etl.linkedpipes.com/resources/jar/"
                + "e-textHolder/0.0.0";

        String iri = "http://etl.linkedpipes.com/resources/components/"
                + "e-textHolder/0.0.0";

        var pluginJarFile = new PluginJarFile(
                new File("e-textHolder-0.0.0.jar"),
                null, jarIri, iri,
                new ArrayList<>(definition),
                new ArrayList<>(configuration),
                new ArrayList<>(description),
                dialogs);

        PluginTemplateLoader loader = new PluginTemplateLoaderMock();
        loader.load(pluginJarFile);

        var definitionActual = loader.getDefinition();
        Assertions.assertEquals(
                iri, definitionActual.resource.stringValue());
        Assertions.assertEquals(
                "Text holder", definitionActual.prefLabel.stringValue());
        Assertions.assertEquals(
                dialogs.size(), definitionActual.dialogs.size());

        var expected = Statements.wrap(
                TestUtils.rdfFromResource(directory + "expected.trig"));
        TestUtils.assertIsomorphic(
                expected.selectByGraph(iri),
                loader.getDefinitionStatements());
        TestUtils.assertIsomorphic(
                expected.selectByGraph(iri + "/configuration"),
                loader.getConfigurationStatements());
        TestUtils.assertIsomorphic(
                expected.selectByGraph(iri + "/configuration-description"),
                loader.getConfigurationDescriptionStatements());
        Assertions.assertEquals(
                dialogs.size(),
                loader.getFiles().size());
    }

}


