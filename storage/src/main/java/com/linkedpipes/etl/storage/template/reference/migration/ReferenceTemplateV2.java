package com.linkedpipes.etl.storage.template.reference.migration;

import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

class ReferenceTemplateV2 {

    /**
     * Remove configuration description and reference to it and delete the
     * configuration description file.
     * <p>
     * We do not need to do any of this on this level.
     */
    public ReferenceTemplate migrateToV3(ReferenceTemplate template) {
        return template;
    }

}
