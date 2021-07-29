package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreService;
import com.linkedpipes.etl.storage.template.store.legacy.LegacyStore;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Migrate data from source to target, it assume that the plugins
 * are already loaded into target.
 */
public class MigrateStore {

    private static final Logger LOG =
            LoggerFactory.getLogger(MigrateStore.class);

    protected final TemplateStore source;

    protected final TemplateStore target;

    protected final StoreInfo info;

    protected File storageDirectory;

    protected Map<String, String> roots = new HashMap<>();

    protected Map<String, String> mappings = new HashMap<>();

    public MigrateStore(
            TemplateStore source, TemplateStore target, StoreInfo info,
            File storageDirectory) {
        this.source = source;
        this.target = target;
        this.info = info;
        this.storageDirectory = storageDirectory;
    }

    public StoreInfo migrate() throws BaseException {
        LOG.info("Migrating store from {} {} to {} {} ...",
                info.repository, info.templateVersion,
                LegacyStore.STORE_NAME, TemplateStoreService.LATEST_VERSION);
        StoreInfo result = info.clone();
        result.templateVersion = TemplateStoreService.LATEST_VERSION;
        loadRoots();
        if (info.templateVersion < 5) {
            migrateMapping();
        }
        for (String reference : source.getReferenceIdentifiers()) {
            migrateReference(reference);
        }
        LOG.info("Migrating store from {} {} to {} {} ... done",
                info.repository, info.templateVersion,
                LegacyStore.STORE_NAME, TemplateStoreService.LATEST_VERSION);
        return result;
    }

    /**
     * Load roots from the reference templates, does not check for existence
     * of the roots (plugin templates).
     */
    protected void loadRoots() throws BaseException {
        Map<String, String> parents = new HashMap<>();
        for (String id : source.getReferenceIdentifiers()) {
            Statements statements = Statements.wrap(
                    source.getReferenceInterface(id));
            Resource resource = TemplateReader.readResource(statements);
            if (resource == null) {
                LOG.error("Can't find resource for: {}", id);
                continue;
            }
            //
            Resource parent = TemplateReader.readParent(resource, statements);
            if (parent == null) {
                LOG.error("Can't read parent for: {}", id);
                continue;
            }
            parents.put(resource.stringValue(), parent.stringValue());
        }
        // Convert parents to the roots.
        roots = new HashMap<>();
        for (String identifier : parents.keySet()) {
            String root = identifier;
            while (parents.containsKey(root)) {
                String ancestor = parents.get(root);
                if (ancestor.equals(root)) {
                    // We reach the root.
                    break;
                }
                root = ancestor;
            }
            roots.put(identifier, root);
        }
    }

    /**
     * For store version 4 the mapping is stored in extra file.
     */
    protected void migrateMapping() throws BaseException {
        File source = new File(storageDirectory, "knowledge");
        File target = new File(storageDirectory,  "templates/v4-knowledge");
        if (source.exists()) {
            try {
                Files.move(source.toPath(), target.toPath());
            } catch (IOException ex) {
                throw new StoreException("Can't move mapping file", ex);
            }
        }
        File mappingFile = new File(target, "mapping.trig");
        if (mappingFile.exists()) {
            loadMapping(mappingFile);
        }
    }

    protected void loadMapping(File file) throws BaseException {
        Statements content = Statements.arrayList();
        try {
            content.addAll(file);
        } catch (IOException ex) {
            throw new BaseException("Can't read mapping file.", ex);
        }
        content.stream().filter(st -> st.getPredicate().equals(OWL.SAMEAS))
                .forEach(st -> {
                    String remote = st.getSubject().stringValue();
                    String local = st.getObject().stringValue();
                    mappings.put(local, remote);
                });
    }

    protected void migrateReference(String id) throws BaseException {
        ReferenceContainer container = loadReferenceToContainer(id);
        int version = info.templateVersion;
        MigrateTemplate migrateTemplate = new MigrateTemplate(
                roots::get, mappings::get);
        container = migrateTemplate.migrateReferenceTemplate(
                container, version);
        // Store to the new repository. The interface and
        // definition are the same for reference templates.
        target.setReferenceInterface(id, container.definitionStatements);
        target.setReferenceDefinition(id, container.definitionStatements);
        target.setReferenceConfiguration(id, container.configurationStatements);
    }

    protected ReferenceContainer loadReferenceToContainer(
            String id) throws StoreException {
        ReferenceContainer result = new ReferenceContainer();
        result.definitionStatements = Statements.set();
        result.definitionStatements.addAll(source.getReferenceDefinition(id));
        result.definitionStatements.addAll(source.getReferenceInterface(id));
        result.configurationStatements = Statements.set();
        result.configurationStatements.addAll(source.getReferenceConfiguration(id));
        result.resource = TemplateReader.readResource(result.definitionStatements);
        return result;
    }

}
