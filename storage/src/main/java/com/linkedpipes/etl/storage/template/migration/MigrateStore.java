package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
import com.linkedpipes.etl.storage.template.store.TemplateStoreService;
import com.linkedpipes.etl.storage.template.store.legacy.LegacyStore;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Migrate data from source to target, it assume that the plugins
 * are already loaded into target.
 */
public class MigrateStore implements TemplatesInformation {

    private static final Logger LOG =
            LoggerFactory.getLogger(MigrateStore.class);

    protected final TemplateStore source;

    protected final TemplateStore target;

    protected final StoreInfo info;

    protected Map<String, String> roots = new HashMap<>();

    public MigrateStore(
            TemplateStore source, TemplateStore target, StoreInfo info) {
        this.source = source;
        this.target = target;
        this.info = info;
    }

    public StoreInfo migrate() throws BaseException {
        LOG.info("Migrating store from {} {} to {} {} ...",
                info.repository, info.templateVersion,
                LegacyStore.STORE_NAME, TemplateStoreService.LATEST_VERSION);
        StoreInfo result = info.clone();
        result.templateVersion = TemplateStoreService.LATEST_VERSION;
        loadRoots();
        for (RepositoryReference reference : source.getReferences()) {
            if (Template.Type.JAR_TEMPLATE.equals(reference.getType())) {
                continue;
            }
            migrateReference(reference);
        }
        LOG.info("Migrating store from {} {} to {} {} ... done",
                info.repository, info.templateVersion,
                LegacyStore.STORE_NAME, TemplateStoreService.LATEST_VERSION);
        return result;
    }

    protected void loadRoots()
            throws BaseException {
        Map<String, String> parents = new HashMap<>();
        for (RepositoryReference reference : source.getReferences()) {
            Statements statements =
                    Statements.wrap(source.getInterface(reference));
            Resource resource = TemplateReader.readResource(statements);
            if (resource == null) {
                LOG.error("Can't find resource for: {}", reference.getId());
                continue;
            }
            //
            Resource parent;
            if (isReference(reference)) {
                parent = TemplateReader.readParent(resource, statements);
            } else {
                parent = resource;
            }
            if (parent == null) {
                LOG.error("Can't read parent for: {}", reference.getId());
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
                if (ancestor == root) {
                    // We reach the root.
                    break;
                }
                root = ancestor;
            }
            roots.put(identifier, root);
        }
    }

    protected boolean isReference(RepositoryReference reference) {
        return Template.Type.REFERENCE_TEMPLATE.equals(reference.getType());
    }

    @Override
    public String getRoot(String identifier) {
        return roots.get(identifier);
    }

    protected void migrateReference(RepositoryReference reference)
            throws BaseException {
        ReferenceContainer container = loadToContainer(reference);
        int version = info.templateVersion;
        MigrateTemplate migrateTemplate = new MigrateTemplate(this);
        container = migrateTemplate.migrateReferenceTemplate(
                container, version);
        // Store to the new repository. The interface and
        // definition are the same for reference templates.
        target.setInterface(reference, container.definition);
        target.setDefinition(reference, container.definition);
        target.setConfig(reference, container.configuration);
    }

    protected ReferenceContainer loadToContainer(
            RepositoryReference reference) throws StoreException {
        ReferenceContainer result = new ReferenceContainer();
        result.definition = Statements.set();
        result.definition.addAll(source.getInterface(reference));
        result.definition.addAll(source.getDefinition(reference));
        result.configuration = Statements.set();
        result.configuration.addAll(source.getConfig(reference));
        result.resource = TemplateReader.readResource(result.definition);
        return result;
    }

}
