package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.template.plugin.PluginContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class TemplateEventNotifier implements TemplateEventListener {

    protected List<TemplateEventListener> listeners = new ArrayList<>();

    public void addListener(TemplateEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onPluginLoaded(PluginJarFile jarFile) {
        for (TemplateEventListener listener : listeners) {
            listener.onPluginLoaded(jarFile);
        }
    }

    @Override
    public void onPluginTemplateLoaded(PluginContainer container) {
        for (TemplateEventListener listener : listeners) {
            listener.onPluginTemplateLoaded(container);
        }
    }

    @Override
    public void onReferenceTemplateLoaded(ReferenceContainer container) {
        for (TemplateEventListener listener : listeners) {
            listener.onReferenceTemplateLoaded(container);
        }
    }

    @Override
    public void onReferenceTemplateChanged(
            ReferenceDefinition oldDefinition,
            ReferenceDefinition newDefinition) {
        for (TemplateEventListener listener : listeners) {
            listener.onReferenceTemplateChanged(oldDefinition, newDefinition);
        }
    }

    @Override
    public void onReferenceTemplateConfigurationChanged(
            String iri, Collection<Statement> configuration) {
        for (TemplateEventListener listener : listeners) {
            listener.onReferenceTemplateConfigurationChanged(
                    iri, configuration);
        }
    }

    @Override
    public void onReferenceTemplateDeleted(String iri) {
        for (TemplateEventListener listener : listeners) {
            listener.onReferenceTemplateDeleted(iri);
        }
    }

}
