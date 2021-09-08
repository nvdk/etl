package com.linkedpipes.etl.storage;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestUtils {

    private static final ClassLoader loader =
            Thread.currentThread().getContextClassLoader();

    public static File fileFromResource(String fileName) {
        URL url = loader.getResource(fileName);
        if (url == null) {
            throw new RuntimeException(
                    "Required resource '" + fileName + "' is missing.");
        }
        return new File(url.getPath());
    }

    public static Collection<Statement> rdfFromResource(String fileName) {
        File file = fileFromResource(fileName);
        return read(file, getFormat(file));
    }

    protected static RDFFormat getFormat(File file) {
        return Rio.getParserFormatForFileName(file.getName()).orElseThrow(
                () -> new RuntimeException("Invalid RDF type for file."));
    }

    protected static Collection<Statement> read(File file, RDFFormat format) {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream, format);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected static Collection<Statement> read(
            InputStream inputStream, RDFFormat format) {
        List<Statement> statements = new ArrayList<>();
        try {
            RDFParser reader = Rio.createParser(format,
                    SimpleValueFactory.getInstance());
            StatementCollector collector
                    = new StatementCollector(statements);
            reader.setRDFHandler(collector);
            reader.parse(inputStream, "http://localhost/base");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException("Can't close stream.", ex);
            }
        }
        return statements;
    }

    public static void assertIsomorphic(
            Collection<Statement> expected, Collection<Statement> actual) {
        boolean isomorphic = Models.isomorphic(actual, expected);
        if (!isomorphic) {
            Set<Statement> actualSet = new HashSet<>(actual);
            Set<Statement> expectedSet = new HashSet<>(expected);
            System.out.println("Missing:");
            int missingCount = 0;
            for (Statement statement : expectedSet) {
                if (actualSet.contains(statement)) {
                    continue;
                }
                System.out.println(
                        "- [" + statement.getContext() + "] "
                                + statement.getSubject() + " "
                                + statement.getPredicate() + " "
                                + statement.getObject());
                missingCount += 1;
            }
            System.out.println("Extra:");
            int extraCount = 0;
            for (Statement statement : actualSet) {
                if (expectedSet.contains(statement)) {
                    continue;
                }
                System.out.println(
                        "+ [" + statement.getContext() + "] "
                                + statement.getSubject() + " "
                                + statement.getPredicate() + " "
                                + statement.getObject());
                extraCount += 1;
            }
            System.out.println("Size expected: " + expectedSet.size()
                    + " actual: " + actualSet.size() + " missing: "
                    + missingCount + " extra: " + extraCount);

        }
        Assertions.assertTrue(isomorphic);
    }

}
