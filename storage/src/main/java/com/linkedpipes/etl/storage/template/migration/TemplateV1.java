package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.reference.ReferenceContainer;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class TemplateV1 {

    private static class Mapping {

        private final String source;

        private final String target;

        public Mapping(String source, String target) {
            this.source = source;
            this.target = target;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(TemplateV1.class);

    private static final Map<String, Mapping> MAPPING;

    static {
        MAPPING = new HashMap<>();

        String prefix = "http://etl.linkedpipes.com/resources/components/";
        String suffix = "/0.0.0";

        MAPPING.put(prefix + "l-sparqlEndpointChunked" + suffix,
                new Mapping("l-sparqlEndpoint", "l-sparqlEndpointChunked"));
        MAPPING.put(prefix + "t-filesToRdfChunked" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfChunked"));
        MAPPING.put(prefix + "t-sparqlConstructChunked" + suffix,
                new Mapping("t-sparqlConstruct", "t-sparqlConstructChunked"));
        MAPPING.put(prefix + "t-filesToRdfGraph" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfGraph"));
        MAPPING.put(prefix + "t-filesToRdfChunked" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfChunked"));
        MAPPING.put(prefix + "t-mustacheChunked" + suffix,
                new Mapping("t-mustache", "t-mustacheChunked"));
        MAPPING.put(prefix + "t-sparqlUpdateChunked" + suffix,
                new Mapping("t-sparqlUpdate", "t-sparqlUpdateChunked"));
        MAPPING.put(prefix + "t-tabularChunked" + suffix,
                new Mapping("t-tabular", "t-tabularChunked"));
    }

    protected final TemplatesInformation templates;

    public TemplateV1(TemplatesInformation templates) {
        this.templates = templates;
    }

    /**
     * There used to be a configuration description stored, now there this
     * is not allowed for reference templates. It is thus not needed
     * to update it. So we only update the configuration - as before some
     * templates shared vocabulary.
     */
    public ReferenceContainer migrateToV2(ReferenceContainer template)
            throws BaseException {
        Resource resource = template.resource;
        String root;
        try {
            root = templates.getRoot(resource.stringValue());
        } catch (BaseException ex) {
            LOG.error("Can't find root for '{}'", resource);
            throw new BaseException("Can't get root template.", ex);
        }
        if (!shouldUpdate(root)) {
            return template;
        }
        Collection<Statement> configuration = updateConfiguration(
                template.configuration, root);
        return new ReferenceContainer(
                template.resource, template.definition,
                Statements.wrap(configuration));
    }

    protected boolean shouldUpdate(String iri) {
        return MAPPING.containsKey(iri);
    }

    protected Collection<Statement> updateConfiguration(
            Collection<Statement> statements, String coreTemplate) {
        Mapping mapping = MAPPING.get(coreTemplate);
        if (mapping == null) {
            return statements;
        }
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        return statements.stream().map((statement) -> {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                return valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        updateIRI((IRI) statement.getObject(), mapping),
                        statement.getContext()
                );
            }
            return valueFactory.createStatement(
                    statement.getSubject(),
                    updateIRI(statement.getPredicate(), mapping),
                    statement.getObject(),
                    statement.getContext());
        }).collect(Collectors.toList());
    }

    protected IRI updateIRI(IRI source, Mapping mapping) {
        return SimpleValueFactory.getInstance().createIRI(
                source.stringValue().replace(mapping.source, mapping.target));
    }

    public static Resource loadParent(
            Resource resource, Statements statements) {
        return TemplateV0.loadParent(resource, statements);
    }

    public static Integer loadVersion(
            Resource resource, Statements statements) {
        // There was no information about version.
        return null;
    }

    public static Resource loadConfiguration(
            Resource resource, Statements statements) {
        return TemplateV0.loadConfiguration(resource, statements);
    }

}
