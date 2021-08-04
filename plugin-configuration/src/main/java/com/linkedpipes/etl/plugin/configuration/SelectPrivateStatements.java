package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SelectPrivateStatements {

    public List<Statement> selectPrivate(
            ConfigurationDescriptionDefinition description,
            List<Statement> statements) {
        Set<IRI> privatePredicates = description.members.stream()
                .filter(member -> member.isPrivate.booleanValue())
                .map(member -> member.property)
                .collect(Collectors.toSet());
        if (privatePredicates.isEmpty()) {
            return Collections.emptyList();
        }
        return statements.stream().filter(
                (st) -> privatePredicates.contains(st.getPredicate()))
                .collect(Collectors.toList());
    }

}
