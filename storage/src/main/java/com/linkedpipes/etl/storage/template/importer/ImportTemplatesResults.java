package com.linkedpipes.etl.storage.template.importer;

import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of template import regardless of source.
 */
public class ImportTemplatesResults {

    /**
     * List of templates IRI that were not imported.
     */
    public List<Resource> ignoredTemplates = new ArrayList<>();

    /**
     * List of local templates IRI that were changed as a result of import
     * operation.
     */
    public List<Resource> updatedTemplates = new ArrayList<>();

    /**
     * Mapping from the template IRI to local template IRI, regardless
     * of the fact if the template was imported or mapped.
     */
    public Map<Resource, Resource> localizedTemplates = new HashMap<>();

}
