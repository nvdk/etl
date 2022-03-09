package com.linkedpipes.etl.storage.store;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.store.ReadOnlyLegacyStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ReadOnlyLegacyTemplateStoreTest {

    @Test
    public void loadV1() throws StorageException {
        File directory = TestUtils.file("migration/v1/");
        var store = new ReadOnlyLegacyStore(
                directory, "http://localhost:8080", 1);
        store.initialize();
        var templates = store.listReferencesTemplate();
        Assertions.assertEquals(1, templates.size());
        Assertions.assertEquals(
                "http://localhost:8080/resources/components/1621865731462",
                templates.iterator().next());
        var pluginOptional = store.loadReferenceTemplate("1621865731462");
        Assertions.assertNotNull(pluginOptional.isPresent());
        var plugin = pluginOptional.get();
        var iri = "http://localhost:8080/resources/components/1621865731462";
        Assertions.assertEquals(iri, plugin.resource().stringValue());
        Assertions.assertEquals(1, plugin.version().intValue());
    }

    @Test
    public void loadV3() throws StorageException {
        File directory = TestUtils.file("migration/v3/");
        var store = new ReadOnlyLegacyStore(
                directory, "http://localhost:8080", 3);
        store.initialize();
        var templates = store.listReferencesTemplate();
        Assertions.assertEquals(1, templates.size());
        var iri = templates.iterator().next();
        var pluginOptional = store.loadReferenceTemplate(iri);
        Assertions.assertTrue(pluginOptional.isPresent());
        var plugin = pluginOptional.get();
        Assertions.assertNotNull(plugin);
        Assertions.assertEquals(3, plugin.version().intValue());
        Assertions.assertEquals(
                "http://linkedpipes.com/component",
                plugin.knownAs().stringValue());
    }

}
