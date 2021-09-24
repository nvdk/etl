package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.store.StoreException;
import com.linkedpipes.etl.storage.template.store.StoreInfo;
import com.linkedpipes.etl.storage.template.store.TemplateStore;
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

    protected final StoreInfo sourceInfo;

    protected File storageDirectory;

    protected Map<String, String> roots = new HashMap<>();

    protected Map<String, String> mappings = new HashMap<>();

    public MigrateStore(
            TemplateStore source, TemplateStore target, StoreInfo sourceInfo,
            File storageDirectory) {
        this.source = source;
        this.target = target;
        this.sourceInfo = sourceInfo;
        this.storageDirectory = storageDirectory;
    }

    public void migrate() throws BaseException {
        LOG.info("Migrating store from {} : {} to {} : {} ...",
                source.getName(), sourceInfo.templateVersion,
                target.getName(), StoreInfo.LATEST_TEMPLATE_VERSION);
        loadRoots();
        if (sourceInfo.templateVersion < 5) {
            migrateMapping();
        }
        for (String iri : source.getReferencesIri()) {
            migrateReference(iri);
        }
        LOG.info("Migrating store from {} : {} to {} : {} ... done",
                source.getName(), sourceInfo.templateVersion,
                target.getName(), StoreInfo.LATEST_TEMPLATE_VERSION);
    }

    /**
     * Load roots from the reference templates, does not check for existence
     * of the roots (plugin templates).
     */
    protected void loadRoots() throws BaseException {
        Map<String, String> parents = new HashMap<>();
        for (String id : source.getReferencesIri()) {
            Statements statements = Statements.wrap(
                    source.getReferenceDefinition(id));
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
        File target = new File(storageDirectory, "templates/v4-knowledge");
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

    protected void migrateReference(String iri) throws BaseException {
        ReferenceContainer container = loadReferenceToContainer(iri);
        int version = sourceInfo.templateVersion;
        MigrateTemplate migrateTemplate = new MigrateTemplate(
                roots::get, mappings::get);
        container = migrateTemplate.migrateReferenceTemplate(
                container, version);
        // Store to the new repository. The interface and
        // definition are the same for reference templates. We
        // also use the resource as an identifier.
        target.setReferenceDefinition(
                container.resource.stringValue(),
                container.definitionStatements);
        target.setReferenceConfiguration(
                container.resource.stringValue(),
                container.configurationStatements);
    }

    protected ReferenceContainer loadReferenceToContainer(
            String iri) throws StoreException {
        ReferenceContainer result = new ReferenceContainer();
        result.definitionStatements = Statements.set();
        result.definitionStatements.addAll(source.getReferenceDefinition(iri));
        result.configurationStatements = Statements.set();
        result.configurationStatements.addAll(
                source.getReferenceConfiguration(iri));
        result.resource = TemplateReader.readResource(
                result.definitionStatements);
        return result;
    }

}
