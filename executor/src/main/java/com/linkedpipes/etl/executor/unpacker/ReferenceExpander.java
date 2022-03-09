package com.linkedpipes.etl.executor.unpacker;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.unpacker.model.GraphCollection;
import com.linkedpipes.etl.executor.unpacker.model.designer.DesignerComponent;
import com.linkedpipes.etl.executor.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.executor.unpacker.model.template.ReferenceTemplate;
import com.linkedpipes.etl.executor.unpacker.model.template.Template;

import java.util.Arrays;

class ReferenceExpander {

    private GraphCollection graphs;

    private final TemplateSource templateSource;

    private final TemplateExpander expander;

    public ReferenceExpander(
            TemplateSource templateSource, TemplateExpander expander) {
        this.templateSource = templateSource;
        this.expander = expander;
    }

    public ExecutorComponent expand(
            DesignerComponent srcComponent, ReferenceTemplate template)
            throws ExecutorException {
        // The reference template add only a configuration.
        mergeWithTemplate(template, srcComponent);
        DesignerComponent component = new DesignerComponent(srcComponent);
        component.setTemplate(template.getTemplate());
        component.setTypes(Arrays.asList(template.getTemplate()));
        //
        return expander.expand(component);
    }

    private void mergeWithTemplate(
            Template template, DesignerComponent srcComponent)
            throws ExecutorException {
        // Add reference to template configuration and make
        // sure the graph is in graph list.
        String configGraph = template.getConfigGraph();
        srcComponent.getConfigurationGraphs().add(configGraph);
        graphs.put(
                configGraph,
                templateSource.getConfiguration(template.getIri()));
    }

    public void setGraphs(GraphCollection graphs) {
        this.graphs = graphs;
    }

}
