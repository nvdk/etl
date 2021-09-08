package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.template.store.StatementsTemplateStore;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.StoreInfoAdapter;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreFactory;
import com.linkedpipes.etl.storage.utils.Statements;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MigrateStoreTest {

    @Test
    public void migrateV1() throws Exception {
        migrateDirectory("v1");
    }

    protected void migrateDirectory(String version)
            throws Exception {
        File storeDirectory =
                TestUtils.fileFromResource("migration/" + version);

        TemplateStoreFactory storeFactory =
                new TemplateStoreFactory(storeDirectory);
        StoreInfo sourceInfo = StoreInfoAdapter.loadForStore(storeDirectory);
        TemplateStore source = storeFactory.createStore(sourceInfo);
        StatementsTemplateStore target = new StatementsTemplateStore();

        (new MigrateStore(source, target, sourceInfo, storeDirectory))
                .migrate();

        Collection<Statement> expectedStatements =
                TestUtils.rdfFromResource(
                        "migration/" + version + "/expected-templates.trig");

        TestUtils.assertIsomorphic(expectedStatements, target.getStatements());
    }

    @Test
    public void migrateV2() throws Exception {
        migrateDirectory("v2");
    }

    @Test
    public void migrateV3() throws Exception {
        migrateDirectory("v3");
    }

    @Test
    public void migrateV4() throws Exception {
        migrateDirectory("v4");
    }

}
