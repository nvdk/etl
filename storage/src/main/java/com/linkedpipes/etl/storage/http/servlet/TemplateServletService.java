package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.plugin.PluginApi;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.template.TemplateApi;
import com.linkedpipes.etl.storage.template.plugin.adapter.PluginTemplateAdapter;
import com.linkedpipes.etl.storage.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.template.reference.adapter.ReferenceTemplateAdapter;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.util.List;
import java.util.Optional;

public class TemplateServletService extends ServletService {

    @FunctionalInterface
    private interface Consumer<T> {

        Response apply(HttpServletRequest request, T content)
                throws StorageException;

    }

    private final PluginApi pluginApi;

    private final TemplateApi templateApi;

    public TemplateServletService(
            PluginApi pluginApi, TemplateApi templateApi) {
        this.pluginApi = pluginApi;
        this.templateApi = templateApi;
    }

    public Response listTemplates(HttpServletRequest request)
            throws StorageException {
        Statements result = Statements.arrayList();
        for (String iri : templateApi.listPluginTemplates()) {
            Optional<PluginTemplate> templateOptional =
                    templateApi.loadPluginTemplate(iri);
            if (templateOptional.isEmpty()) {
                continue;
            }
            result.addAll(PluginTemplateAdapter.definitionAsRdf(
                    templateOptional.get()));
        }
        for (String iri : templateApi.listReferenceTemplates()) {
            Optional<ReferenceTemplate> templateOptional =
                    templateApi.loadReferenceTemplate(iri);
            if (templateOptional.isEmpty()) {
                continue;
            }
            result.addAll(ReferenceTemplateAdapter.definitionAsRdf(
                    templateOptional.get()));
        }
        return responseStatements(request, result);
    }


    public Response templateDefinition(HttpServletRequest request, String iri)
            throws StorageException {
        return withTemplate(
                request, iri,
                this::pluginDefinition,
                this::referenceDefinition);
    }

    protected Response withTemplate(
            HttpServletRequest request, String iri,
            Consumer<PluginTemplate> pluginConsumer,
            Consumer<ReferenceTemplate> referenceConsumer)
            throws StorageException {
        if (templateApi.isPluginTemplate(iri)) {
            Optional<PluginTemplate> templateOptional =
                    templateApi.loadPluginTemplate(iri);
            if (templateOptional.isEmpty()) {
                return responseNotFound();
            }
            return pluginConsumer.apply(request, templateOptional.get());
        } else if (templateApi.isReferenceTemplate(iri)) {
            Optional<ReferenceTemplate> templateOptional =
                    templateApi.loadReferenceTemplate(iri);
            if (templateOptional.isEmpty()) {
                return responseNotFound();
            }
            return referenceConsumer.apply(request, templateOptional.get());
        } else {
            return responseNotFound();
        }
    }

    protected Response pluginDefinition(
            HttpServletRequest request, PluginTemplate template) {
        Statements content = PluginTemplateAdapter.definitionAsRdf(template);
        return responseStatements(request, content);
    }

    protected Response referenceDefinition(
            HttpServletRequest request, ReferenceTemplate template) {
        Statements content = ReferenceTemplateAdapter.definitionAsRdf(template);
        return responseStatements(request, content);
    }

    public Response configurationDescription(
            HttpServletRequest request, String iri) throws StorageException {
        return withTemplate(
                request, iri,
                this::pluginConfigurationDescription,
                this::referenceConfigurationDescription);
    }

    protected Response pluginConfigurationDescription(
            HttpServletRequest request, PluginTemplate template) {
        Statements content =
                PluginTemplateAdapter.configurationDescriptionAsRdf(template);
        return responseStatements(request, content);
    }

    protected Response referenceConfigurationDescription(
            HttpServletRequest request, ReferenceTemplate template)
            throws StorageException {
        Optional<PluginTemplate> pluginTemplate =
                templateApi.loadPluginTemplate(
                        template.pluginTemplate().stringValue());
        if (pluginTemplate.isEmpty()) {
            return responseNotFound();
        }
        return pluginConfigurationDescription(request, pluginTemplate.get());
    }

    public Response configuration(
            HttpServletRequest request, String iri) throws StorageException {
        return withTemplate(
                request, iri,
                this::pluginConfiguration,
                this::referenceConfiguration);
    }

    protected Response pluginConfiguration(
            HttpServletRequest request, PluginTemplate template) {
        Statements content = PluginTemplateAdapter.configurationAsRdf(template);
        return responseStatements(request, content);
    }

    protected Response referenceConfiguration(
            HttpServletRequest request, ReferenceTemplate template) {
        Statements content =
                ReferenceTemplateAdapter.configurationAsRdf(template);
        return responseStatements(request, content);
    }

    public Response newConfiguration(
            HttpServletRequest request, String iri) throws StorageException {
        Optional<Statements> contentOptional =
                templateApi.getNewConfiguration(iri);
        if (contentOptional.isEmpty()) {
            return responseNotFound();
        }
        return responseStatements(request, contentOptional.get());
    }

    public Response effectiveConfiguration(
            HttpServletRequest request, String iri) throws StorageException {
        Optional<Statements> contentOptional =
                templateApi.getEffectiveConfiguration(iri);
        if (contentOptional.isEmpty()) {
            return responseNotFound();
        }
        return responseStatements(request, contentOptional.get());
    }


    public Response templateFile(
            HttpServletRequest request, String iri, String path)
            throws StorageException {
        return withTemplate(
                request, iri,
                (req, template) -> templateFile(req, template, path),
                (req, template) -> referenceFile(req, template, path));
    }

    protected Response templateFile(
            HttpServletRequest request, PluginTemplate template,
            String path) throws StorageException {
        Optional<byte[]> contentOptional = pluginApi.getPluginFile(
                template.resource().stringValue(),path);
        if (contentOptional.isEmpty()) {
            return responseNotFound();
        }
        return responseStatementsByteArray(contentOptional.get());
    }

    protected Response responseStatementsByteArray(byte[] content) {
        // TODO We need to detect file type from name.
        Response.ResponseBuilder result = Response.ok();
        result.header("content-type", "text/plain");

        StreamingOutput output = (stream) -> {
            stream.write(content);
        };

        return result.entity(output).build();
    }

    protected Response referenceFile(
            HttpServletRequest request, ReferenceTemplate template,
            String path) throws StorageException {
        if (template.pluginTemplate() == null) {
            return responseNotFound();
        }
        Optional<byte[]> contentOptional = pluginApi.getPluginFile(
                template.pluginTemplate().stringValue(),path);
        if (contentOptional.isEmpty()) {
            return responseNotFound();
        }
        return responseStatementsByteArray(contentOptional.get());
    }

    public Response storeTemplate(
            HttpServletRequest request, StatementsSelector templateRdf)
            throws StorageException {
        List<ReferenceTemplate> references =
                ReferenceTemplateAdapter.asReferenceTemplates(templateRdf);
        if (references.size() != 1) {
            throw new StorageException(
                    "Invalid number of reference templates given. {}",
                    references.size());
        }
        ReferenceTemplate stored =
                templateApi.storeReferenceTemplate(references.get(0));
        return Response.ok()
                .header("Location", stored.resource().stringValue())
                .build();
    }

    public Response deleteTemplate(String iri) throws StorageException {
        templateApi.deleteReferenceTemplate(iri);
        return Response.ok().build();
    }

}
