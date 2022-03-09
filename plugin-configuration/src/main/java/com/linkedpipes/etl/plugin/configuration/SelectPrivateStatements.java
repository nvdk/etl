package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.plugin.configuration.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SelectPrivateStatements {

    public List<Statement> selectPrivate(
            ConfigurationDescription description,
            Collection<Statement> statements) {
        Set<IRI> privatePredicates = description.members.stream()
                .filter(member -> member.isPrivate != null)
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
