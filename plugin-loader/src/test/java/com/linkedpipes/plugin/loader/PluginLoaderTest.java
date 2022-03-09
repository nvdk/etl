package com.linkedpipes.plugin.loader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class PluginLoaderTest {

    @Test
    public void loadTextHolder() throws Exception {
        File file = TestUtils.fileFromResource("e-textHolder-0.0.0.jar");
        PluginLoader loader = new PluginLoader();
        List<Plugin> actual = loader.loadPlugin(file);
        //
        Assertions.assertEquals(1, actual.size());
        Plugin plugin = actual.get(0);
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/"
                        + "e-textHolder/0.0.0",
                plugin.pluginIri);
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/jars/e-textHolder/0.0.0",
                plugin.jarIri);
        Assertions.assertEquals(
                26, plugin.definition.size());
        Assertions.assertEquals(
                5, plugin.configuration.size());
        Assertions.assertEquals(
                10, plugin.configurationDescription.size());
        Assertions.assertEquals(
                6, plugin.fileEntries.size());
        var dialogs = plugin.fileEntries;
        Assertions.assertTrue(dialogs.containsKey("config/dialog.js"));
        Assertions.assertTrue(dialogs.containsKey("config/dialog.html"));
        Assertions.assertTrue(dialogs.containsKey("template/dialog.js"));
        Assertions.assertTrue(dialogs.containsKey("template/dialog.html"));
        Assertions.assertTrue(dialogs.containsKey("instance/dialog.js"));
        Assertions.assertTrue(dialogs.containsKey("instance/dialog.html"));
        // Try to copy out a file.
        File actualFile = Files.createTempFile("lp-etl-text", "").toFile();
        loader.copyFile(
                plugin, dialogs.get("instance/dialog.html"), actualFile);
        String actualFileContent = Files.readString(actualFile.toPath());
        actualFile.delete();
        Assertions.assertEquals(
                "<lp-dialog-control-instance "
                        + "lp-dialog=\"dialog\" "
                        + "lp-application=\"application\">"
                        + "</lp-dialog-control-instance>\n",
                actualFileContent);
    }

}
