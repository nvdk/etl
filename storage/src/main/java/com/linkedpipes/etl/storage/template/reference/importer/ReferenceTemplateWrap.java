package com.linkedpipes.etl.storage.template.reference.importer;

import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

class ReferenceTemplateWrap {

    private final ReferenceTemplate wrap;

    public Resource resource;

    public Resource template;

    public Resource knownAs;

    public Literal version;

    public Resource remoteResource;

    public Resource remoteTemplate;

    public boolean imported = false;

    public ReferenceTemplateWrap(ReferenceTemplate template) {
        this.wrap = template;
        this.resource = template.resource();
        this.template = template.template();
        this.knownAs = template.knownAs();
        this.version = template.version();
        this.remoteResource = template.resource();
        this.remoteTemplate = template.template();
    }

    public Resource pluginTemplate() {
        return wrap.pluginTemplate();
    }

    public ReferenceTemplate asReferenceTemplate() {
        return new ReferenceTemplate(
                resource,
                template,
                wrap.prefLabel(),
                wrap.description(),
                wrap.note(),
                wrap.color(),
                wrap.tags(),
                knownAs,
                wrap.pluginTemplate(),
                version,
                wrap.configuration(),
                ReferenceTemplate.defaultConfigurationGraph(resource));
    }

}
