package com.linkedpipes.etl.storage.template.list;

import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateEventListener;
import com.linkedpipes.etl.storage.template.TemplateException;
import com.linkedpipes.etl.storage.template.plugin.PluginContainer;
import com.linkedpipes.etl.storage.template.plugin.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.template.reference.ReferenceDefinition;
import com.linkedpipes.etl.storage.template.reference.ReferenceTemplate;
import com.linkedpipes.etl.storage.template.reference.RootTemplateSource;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TemplateList implements
        TemplateEventListener, RootTemplateSource {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateList.class);

    private final Map<String, Template> templates = new HashMap<>();

    @Override
    public void onPluginTemplateLoaded(PluginContainer container) {
        String iri = container.resource.stringValue();
        PluginTemplate template = new PluginTemplate(container);
        templates.put(iri, template);
    }

    @Override
    public void onReferenceTemplateLoaded(ReferenceContainer container) {
        String iri = container.resource.stringValue();
        ReferenceTemplate template = new ReferenceTemplate(
                container.identifier, container.definition);
        templates.put(iri, template);
    }

    @Override
    public void onReferenceTemplateChanged(
            ReferenceDefinition oldDefinition,
            ReferenceDefinition newDefinition) {
        String iri = newDefinition.resource.stringValue();
        Template oldTemplate = templates.get(iri);
        if (oldTemplate == null) {
            LOG.error("Missing template to update '{}'.", iri);
            return;
        }
        ReferenceTemplate template = new ReferenceTemplate(
                oldTemplate.getId(), newDefinition);
        templates.put(iri, template);
    }

    @Override
    public void onReferenceTemplateDeleted(String iri) {
        templates.remove(iri);
    }

    public Template getTemplate(String iri) {
        return templates.get(iri);
    }

    public Collection<Template> getTemplates() {
        return templates.values();
    }

    public Template getParent(Template template) {
        if (template instanceof ReferenceTemplate) {
            ReferenceTemplate ref = (ReferenceTemplate) template;
            return getTemplate(ref.getTemplate());
        }
        return null;
    }

    public Template getRootTemplate(Template template) {
        if (template instanceof PluginTemplate) {
            return template;
        } else if (template instanceof ReferenceTemplate) {
            ReferenceTemplate referenceTemplate = (ReferenceTemplate) template;
            return getTemplate(referenceTemplate.getRootPluginTemplate());
        } else {
            throw new RuntimeException("Unknown component type.");
        }
    }

    /**
     * The path from root (template) to the given template.
     */
    public List<Template> getAncestors(Template template) {
        LinkedList<Template> templates = collectAncestors(template);
        Collections.reverse(templates);
        return templates;
    }

    private LinkedList<Template> collectAncestors(Template template) {
        LinkedList<Template> output = new LinkedList<>();
        while (true) {
            output.add(template);
            if (template.isPluginTemplate()) {
                break;
            } else if (template.isReferenceTemplate()) {
                ReferenceTemplate reference = (ReferenceTemplate) template;
                template = getTemplate(reference.getTemplate());
                if (template == null) {
                    LOG.warn("Missing template for: {}", reference.getIri());
                    break;
                }
            } else {
                throw new RuntimeException("Unknown template type: "
                        + template.getId());
            }
        }
        return output;
    }

    public List<Template> getAncestorsWithoutJarTemplate(Template template) {
        LinkedList<Template> templates = collectAncestors(template);
        templates.remove(templates.removeLast());
        Collections.reverse(templates);
        return templates;
    }

    public Collection<Template> getSuccessors(Template template) {
        Map<Template, List<Template>> children = buildChildrenIndex();
        Set<Template> output = new HashSet<>();
        Set<Template> toTest = new HashSet<>();
        toTest.addAll(children.getOrDefault(
                template, Collections.EMPTY_LIST));
        while (!toTest.isEmpty()) {
            Template item = toTest.iterator().next();
            toTest.remove(item);
            if (output.contains(item)) {
                continue;
            }
            List<Template> itemChildren = children.getOrDefault(
                    item, Collections.EMPTY_LIST);
            output.add(item);
            output.addAll(itemChildren);
            toTest.addAll(itemChildren);
        }
        return output;
    }

    private Map<Template, List<Template>> buildChildrenIndex() {
        Map<Template, List<Template>> children = new HashMap<>();
        for (Template item : getTemplates()) {
            if (!item.isReferenceTemplate()) {
                continue;
            }
            ReferenceTemplate reference = (ReferenceTemplate) item;
            Template parent = getTemplate(reference.getTemplate());
            List<Template> brothers = children.computeIfAbsent(
                    parent, key -> new LinkedList<>());
            // Create if does not exists.
            brothers.add(reference);
        }
        return children;
    }

    @Override
    public String getRootTemplate(String iri) throws TemplateException {
        Template template = getTemplate(iri);
        if (template == null) {
            throw new TemplateException("Missing template");
        }
        return getRootTemplate(template).getIri();
    }

}
