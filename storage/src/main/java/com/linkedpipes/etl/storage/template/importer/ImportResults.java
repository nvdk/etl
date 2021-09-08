package com.linkedpipes.etl.storage.template.importer;

import java.util.List;
import java.util.Map;

/**
 * Result of template import regardless of source.
 */
public class ImportResults {

    /**
     * List of templates IRI that were not imported.
     */
    public List<String> ignoredTemplates;

    /**
     * List of local templates IRI that were changed as a result of import
     * operation.
     */
    public List<String> updatedTemplates;

    /**
     * Mapping from the template IRI to local template IRI, regardless
     * of the fact if the template was imported or not.
     */
    public Map<String, String> importedTemplates;

}
