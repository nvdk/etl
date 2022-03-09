package com.linkedpipes.etl.executor.unpacker;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.unpacker.model.GraphCollection;
import com.linkedpipes.etl.executor.unpacker.model.ModelLoader;
import com.linkedpipes.etl.executor.unpacker.model.designer.DesignerComponent;
import com.linkedpipes.etl.executor.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.executor.unpacker.model.template.JarTemplate;
import com.linkedpipes.etl.executor.unpacker.model.template.ReferenceTemplate;
import com.linkedpipes.etl.executor.unpacker.model.template.Template;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

class TemplateExpander {

    private final TemplateSource templateSource;

    private final JarExpander jarExpander;

    private final ReferenceExpander referenceExpander;

    public TemplateExpander(TemplateSource templateSource) {
        this.templateSource = templateSource;
        jarExpander = new JarExpander(templateSource);
        referenceExpander = new ReferenceExpander(templateSource, this);
    }

    public void setGraphs(GraphCollection graphs) {
        jarExpander.setGraphs(graphs);
        referenceExpander.setGraphs(graphs);
    }

    public ExecutorComponent expand(DesignerComponent srcComponent)
            throws ExecutorException {

        Template template = getTemplate(srcComponent);
        if (template instanceof JarTemplate) {
            return expandJarTemplate(srcComponent, (JarTemplate) template);
        } else if (template instanceof ReferenceTemplate) {
            return expandReferenceTemplate(srcComponent,
                    (ReferenceTemplate) template);
        } else {
            throw new ExecutorException("Invalid template type: {}",
                    template.getClass().getName());
        }
    }

    private Template getTemplate(DesignerComponent component)
            throws ExecutorException {
        String templateIri = component.getTemplate();
        Collection<Statement> definition =
                templateSource.getDefinition(templateIri);
        return loadTemplate(definition);
    }

    private Template loadTemplate(Collection<Statement> templateAsRdf)
            throws ExecutorException {
        ClosableRdfSource source = Rdf4jSource.wrapInMemory(templateAsRdf);
        try {
            return ModelLoader.loadTemplate(source);
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't load object.", ex);
        } finally {
            source.close();
        }
    }

    private ExecutorComponent expandJarTemplate(
            DesignerComponent srcComponent, JarTemplate template)
            throws ExecutorException {
        ExecutorComponent component = jarExpander.expand(
                srcComponent.getIri(),
                srcComponent.getConfigurationGraphs(),
                template);
        copyBasicInformation(srcComponent, component);
        return component;
    }

    private void copyBasicInformation(
            DesignerComponent sourceComponent,
            ExecutorComponent targetComponent) {
        targetComponent.setLabel(sourceComponent.getLabel());
    }

    private ExecutorComponent expandReferenceTemplate(
            DesignerComponent srcComponent, ReferenceTemplate template)
            throws ExecutorException {
        ExecutorComponent component = referenceExpander.expand(
                srcComponent, template);
        copyBasicInformation(srcComponent, component);
        return component;
    }

}
