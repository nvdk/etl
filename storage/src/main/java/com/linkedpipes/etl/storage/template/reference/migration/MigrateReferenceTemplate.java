package com.linkedpipes.etl.storage.template.reference.migration;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

public class MigrateReferenceTemplate {

    protected final PluginTemplateSource templateSource;

    public MigrateReferenceTemplate(PluginTemplateSource templateSource) {
        this.templateSource = templateSource;
    }

    public ReferenceTemplate migrate(ReferenceTemplate template)
            throws StorageException {
        ReferenceTemplate result = template;
        int version = 0;
        if (template.version() != null) {
            version = template.version().intValue();
        }
        if (version < 1) {
            result = (new ReferenceTemplateV0())
                    .migrateToV1(result);
        }
        if (version < 2) {
            result = (new ReferenceTemplateV1(templateSource))
                    .migrateToV2(result);
        }
        if (version < 3) {
            result = (new ReferenceTemplateV2())
                    .migrateToV3(result);
        }
        if (version < 4) {
            result = (new ReferenceTemplateV3())
                    .migrateToV4(result);
        }
        if (version < 5) {
            result = (new ReferenceTemplateV4(templateSource))
                    .migrateToV5(result);
        }
        return result;
    }

}
