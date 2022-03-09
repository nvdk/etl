package com.linkedpipes.etl.storage.http;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.StatementsFile;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ContentReader {

    public static Statements readHttpStream(
            InputStream stream, String contentType) throws StorageException {
        if (stream == null) {
            return Statements.empty();
        }
        // TODO Next line is not true.
        String mimeType = contentType;
        Optional<RDFFormat> format = Rio.getParserFormatForMIMEType(mimeType);
        if (format.isEmpty()) {
            throw new StorageException(
                    "Can't determine file format: {}", contentType);
        }
        return readHttpStream(stream, format.get());
    }

    public static Statements readHttpStream(
            InputStream stream, RDFFormat format) throws StorageException {
        StatementsFile result = Statements.arrayList().file();
        try {
            result.addAll(stream, format);
        } catch (IOException ex) {
            throw new StorageException("Can't load data from HTTP stream.", ex);
        }
        return result;
    }

    public static Statements readFileStream(
            InputStream stream, String fileName) throws StorageException {
        if (stream == null) {
            return Statements.empty();
        }
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
        if (format.isEmpty()) {
            throw new StorageException("Can't determine file format");
        }
        return readHttpStream(stream, format.get());
    }

}
