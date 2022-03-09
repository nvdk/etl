package com.linkedpipes.etl.storage.store;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class CachedTemplateStoreV1Test {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

//    @Test
//    public void testCachedTemplateStoreV1() throws Exception {
//        File directory = TestUtils.tempDirectory();
//        CachedTemplateStoreV1 store = new CachedTemplateStoreV1(
//                directory, "http://example.com/template/");
//
//        var textHolder = new PluginTemplate();
//        textHolder.resource = valueFactory.createIRI(
//                "http://example/textHolder");
//        store.storePluginTemplate(textHolder);
//        Assertions.assertEquals(1, store.listPluginTemplates().size());
//
//        var fileHolder = new PluginTemplate();
//        fileHolder.resource = valueFactory.createIRI(
//                "http://example/fileHolder");
//        store.storePluginTemplate(fileHolder);
//        Assertions.assertEquals(2, store.listPluginTemplates().size());
//
//        Assertions.assertEquals(
//                textHolder.resource,
//                store.loadPluginTemplate("http://example/textHolder").resource);
//
//        var textTemplate = new ReferenceTemplate();
//        textTemplate.pluginTemplate = textHolder.resource;
//        var textTemplateIri = store.storeReferenceTemplate(textTemplate);
//
//        Assertions.assertEquals(1, store.listReferencesTemplate().size());
//
//        var reference =
//                store.loadReferenceTemplate(textTemplateIri);
//        Assertions.assertEquals(
//                textTemplateIri,
//                reference.resource.stringValue());
//        Assertions.assertEquals(
//                textHolder.resource,
//                textTemplate.pluginTemplate);
//
//        store.deleteReferenceTemplate(textTemplateIri);
//
//        Assertions.assertTrue(store.listReferencesTemplate().isEmpty());
//
//        TestUtils.removeDirectory(directory);
//    }

}
