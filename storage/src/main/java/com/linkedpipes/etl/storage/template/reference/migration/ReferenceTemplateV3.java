package com.linkedpipes.etl.storage.template.reference.migration;

import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;

class ReferenceTemplateV3 {

    /**
     * In this version an explicit reference to the configuration graph
     * was added. So we just set it to default value no matter the previous
     * state.
     */
    public ReferenceTemplate migrateToV4(ReferenceTemplate template) {
        return new ReferenceTemplate(
                template.resource(),
                template.template(),
                template.prefLabel(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                template.knownAs(),
                template.pluginTemplate(),
                template.version(),
                template.configuration(),
                ReferenceTemplate.defaultConfigurationGraph(
                        template.resource()));
    }

}
