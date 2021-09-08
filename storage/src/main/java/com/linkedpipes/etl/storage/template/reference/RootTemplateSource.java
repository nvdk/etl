package com.linkedpipes.etl.storage.template.reference;

import com.linkedpipes.etl.storage.template.TemplateException;
import org.eclipse.rdf4j.model.Resource;

/**
 * Given an IRI of a template return the root template.
 */
public interface RootTemplateSource {

    String getRootTemplate(String iri) throws TemplateException;

}
