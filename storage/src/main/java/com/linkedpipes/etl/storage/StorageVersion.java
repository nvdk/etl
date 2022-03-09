package com.linkedpipes.etl.storage;

import org.eclipse.rdf4j.model.Literal;

public class StorageVersion {

    static public final int CURRENT = 5;

    static public boolean isCurrent(Literal version) {
        return version != null && version.intValue() == CURRENT;
    }

}
