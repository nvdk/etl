package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.template.plugin.PluginContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

public interface TemplateEventListener {

    default void onPluginLoaded(PluginJarFile jarFile) {
        // Do nothing.
    }

    default void onPluginTemplateLoaded(PluginContainer container) {
        // Do nothing.
    }

    /**
     * Called when a new component is created at a runtime.
     */
    default void onReferenceTemplateCreated(ReferenceContainer container) {
        // Do nothing.
    }

    /**
     * Called when a a new component is created by loading it
     * from a store at the start up.
     */
    default void onReferenceTemplateLoaded(ReferenceContainer container) {
        // Do nothing.
    }

    default void onReferenceTemplateChanged(
            ReferenceDefinition oldDefinition,
            ReferenceDefinition newDefinition) {
        // Do nothing.
    }

    default void onReferenceTemplateConfigurationChanged(
            String iri, Collection<Statement> configuration) {
        // Do nothing.
    }

    default void onReferenceTemplateDeleted(String iri) {
        // Do nothing.
    }

}
