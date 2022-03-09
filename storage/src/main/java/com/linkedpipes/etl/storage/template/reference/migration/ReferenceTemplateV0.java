package com.linkedpipes.etl.storage.template.reference.migration;

import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

class ReferenceTemplateV0 {

    /**
     * There were no changes between 0 and 1 from the perspective of
     * templates.
     */
    public ReferenceTemplate migrateToV1(ReferenceTemplate template) {
        return template;
    }

}
