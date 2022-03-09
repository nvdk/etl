package com.linkedpipes.etl.storage.assistant;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record AssistantData(
        /*
         * Tags with number of uses.
         */
        Map<Literal, Integer> tags,
        /*
         * Represent a data connection from one component type to another.
         */
        Map<ComponentPair, Integer> followup
) {

    record ComponentPair(
            Resource source,
            Value sourcePort,
            Resource target,
            Value targetPort
    ) {

    }

}
