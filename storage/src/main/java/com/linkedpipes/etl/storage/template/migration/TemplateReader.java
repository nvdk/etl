package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.storage.utils.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Version agnostic template information reader.
 */
public class TemplateReader {

    protected TemplateReader() {
    }

    public static Resource readResource(Statements statements) {
        Set<Resource> result = new HashSet<>();
        result.addAll(statements.selectIri(
                null, RDF.TYPE, LP.JAR_TEMPLATE)
                .subjects());
        result.addAll(statements.selectIri(
                null, RDF.TYPE, "http://linkedpipes.com/ontology/JarTemplate")
                .subjects());
        result.addAll(statements.selectIri(
                null, RDF.TYPE, LP.REFERENCE_TEMPLATE)
                .subjects());
        result.addAll(statements.selectIri(
                null, RDF.TYPE, "http://linkedpipes.com/ontology/Template")
                .subjects());
        if (result.size() == 1) {
            return result.iterator().next();
        }
        return null;
    }

    public static Resource readParent(
            Resource resource, Statements statements) {
        // There is only one representation of the parent.
        return TemplateV0.loadParent(resource, statements);
    }

    public static Integer readVersion(
            Resource resource, Statements statements, Integer defaultValue) {
        return firstNotNull(
                () -> TemplateV5.loadVersion(resource, statements),
                () -> defaultValue
        );
    }

    public static <T> T firstNotNull(Supplier<T>... suppliers) {
        for (Supplier<T> supplier : suppliers) {
            T value = supplier.get();
            if (value == null) {
                continue;
            }
            return value;
        }
        return null;
    }

    public static Resource readConfiguration(
            Resource resource, Statements statements) {
        // There is only one representation of the configuration.
        return TemplateV0.loadConfiguration(resource, statements);
    }

    public static Resource readKnownAs(
            Resource resource, Statements statements) {
        // The known as is supported only from version 5.
        return TemplateV5.loadKnownAs(resource, statements);
    }

}
