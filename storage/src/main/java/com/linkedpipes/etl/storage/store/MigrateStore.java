package com.linkedpipes.etl.storage.store;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.migration.MigratePipeline;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.template.reference.migration.MigrateReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Migrate reference templates from source to target. In addition, each
 * template is migrated.
 */
public class MigrateStore {

    private static final Logger LOG =
            LoggerFactory.getLogger(MigrateStore.class);

    private final Store source;

    private final Store target;

    private final Map<String, String> pluginTemplateMap = new HashMap<>();

    private final MigrateReferenceTemplate templateMigration;

    private final MigratePipeline pipelineMigration;

    public MigrateStore(Store source, Store target) {
        this.source = source;
        this.target = target;
        this.templateMigration = new MigrateReferenceTemplate(
                iri -> pluginTemplateMap.getOrDefault(iri, iri));
        this.pipelineMigration = new MigratePipeline(
                iri -> pluginTemplateMap.getOrDefault(iri, iri));
    }

    public void migrate() throws StorageException {
        buildPluginTemplateMap();
        for (String iri : source.listReferencesTemplate()) {
            migrateReferenceTemplate(iri);
        }
        for (String iri : source.listPipelines()) {
            migratePipeline(iri);
        }
    }

    private void buildPluginTemplateMap() throws StorageException {
        pluginTemplateMap.clear();
        Map<String, String> parents = new HashMap<>();
        for (String iri : source.listReferencesTemplate()) {
            Optional<ReferenceTemplate> templateOptional =
                    source.loadReferenceTemplate(iri);
            if (templateOptional.isEmpty()) {
                continue;
            }
            ReferenceTemplate template = templateOptional.get();
            parents.put(
                    template.resource().stringValue(),
                    template.template().stringValue());
        }
        // Find the roots.
        for (String identifier : parents.keySet()) {
            String parent = identifier;
            while (parents.containsKey(parent)) {
                String ancestor = parents.get(parent);
                if (ancestor.equals(parent)) {
                    // We reach the root.
                    break;
                }
                parent = ancestor;
            }
            pluginTemplateMap.put(identifier, parent);
        }
    }

    private void migrateReferenceTemplate(String iri) throws StorageException {
        Optional<ReferenceTemplate> templateOptional =
                source.loadReferenceTemplate(iri);
        if (templateOptional.isEmpty()) {
            return;
        }
        ReferenceTemplate template = templateOptional.get();
        ReferenceTemplate result = templateMigration.migrate(template);
        target.storeReferenceTemplate(result);
    }

    private void migratePipeline(String iri) {
        try {
            Optional<Pipeline> pipelineOptional = source.loadPipeline(iri);
            if (pipelineOptional.isEmpty()) {
                return;
            }
            Pipeline pipeline = pipelineOptional.get();
            Pipeline result = pipelineMigration.migratePipeline(pipeline);
            target.storePipeline(result);
        } catch (StorageException | RuntimeException ex) {
            LOG.error("Can't migrate pipeline '{}'. Pipeline was ignored.",
                    iri, ex);
        }
    }

}
