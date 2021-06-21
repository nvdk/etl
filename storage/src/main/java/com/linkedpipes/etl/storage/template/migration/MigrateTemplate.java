package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;

public class MigrateTemplate {

    protected final TemplatesInformation templates;

    public MigrateTemplate(TemplatesInformation templates) {
        this.templates = templates;
    }

    public ReferenceContainer migrateReferenceTemplate(
            ReferenceContainer template, int defaultVersion
    ) throws BaseException {
        ReferenceContainer result = template;
        if (defaultVersion < 1) {
            result = (new TemplateV0()).migrateToV1(result);
        }
        if (defaultVersion < 2) {
            result = (new TemplateV1(templates)).migrateToV2(result);
        }
        if (defaultVersion < 3) {
            result = (new TemplateV2()).migrateToV3(result);
        }
        if (defaultVersion < 4) {
            result = (new TemplateV3()).migrateToV4(result);
        }
        return result;
    }

}
