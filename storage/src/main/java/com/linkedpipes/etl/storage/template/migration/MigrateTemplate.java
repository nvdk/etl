package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinitionAdapter;

public class MigrateTemplate {

    protected final TemplateInformation.RootSource rootSource;

    protected final TemplateInformation.MappingSource mappingSource;

    public MigrateTemplate(
            TemplateInformation.RootSource rootSource,
            TemplateInformation.MappingSource mappingSource) {
        this.rootSource = rootSource;
        this.mappingSource = mappingSource;
    }

    /**
     * All migration function must consume and produce the data as statements.
     * The container.definition should must not be used.
     */
    public ReferenceContainer migrateReferenceTemplate(
            ReferenceContainer template, int defaultVersion
    ) throws BaseException {
        ReferenceContainer container = template;
        if (defaultVersion < 1) {
            container = (new TemplateV0()).migrateToV1(container);
        }
        if (defaultVersion < 2) {
            container = (new TemplateV1(rootSource)).migrateToV2(container);
        }
        if (defaultVersion < 3) {
            container = (new TemplateV2()).migrateToV3(container);
        }
        if (defaultVersion < 4) {
            container = (new TemplateV3()).migrateToV4(container);
        }
        if (defaultVersion < 5) {
            container = (new TemplateV4(rootSource, mappingSource))
                    .migrateToV5(container);
        }
        synchronizeDefinitions(container);
        return container;
    }

    /**
     * Make sure that definition and definitionStatements holds the same
     * information. We use definitionStatements as the main source
     * as it is used by migration.
     */
    protected void synchronizeDefinitions(ReferenceContainer container)
            throws BaseException {
        container.definition = ReferenceDefinitionAdapter.create(
                container.definitionStatements);
        if (container.definition == null) {
            throw new BaseException("Missing template definition.");
        }
        container.definitionStatements =
                ReferenceDefinitionAdapter.asStatements(container.definition);
    }

}
