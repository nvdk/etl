package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This applies only to values not substitutions.
 */
class SelectPrivateStatements {

    public List<Statement> selectPrivate(
            ConfigurationDescription description,
            StatementsSelector statements) {
        Set<IRI> privatePredicates = new HashSet<>();
        // For members with nested entities, predicates private
        // within the entities.
        Map<IRI, Set<IRI>> privateNestedPredicates = new HashMap<>();
        for (var entry : description.members().entrySet()) {
            ConfigurationDescription.Member member = entry.getValue();
            if (member.isPrivate()) {
                privatePredicates.add(entry.getKey());
            }
            if (!member.privateProperties().isEmpty()) {
                privateNestedPredicates.put(
                        entry.getKey(), member.privateProperties());
            }
        }
        if (privatePredicates.isEmpty() && privateNestedPredicates.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Statement> result = new ArrayList<>();
        Collection<Resource> subjects =
                statements.selectByType(
                        description.configurationType()).subjects();
        for (Resource subject : subjects) {
            for (Statement statement : statements.withSubject(subject)) {
                IRI predicate = statement.getPredicate();
                if (privatePredicates.contains(predicate)) {
                    result.add(statement);
                    continue;
                }
                Set<IRI> nested = privateNestedPredicates.get(predicate);
                if (nested != null) {
                    result.addAll(selectInSubTree(
                            statements, statement.getObject(), nested));
                }
            }
        }
        return result;
    }

    /**
     * Select statements with given predicates from the subtree rooted
     * in given value.
     */
    private List<Statement> selectInSubTree(
            StatementsSelector statements, Value root, Set<IRI> predicates) {
        List<Statement> result = new ArrayList<>();
        if (!root.isResource()) {
            return result;
        }
        Set<Resource> visited = new HashSet<>();
        Deque<Resource> toVisit = new ArrayDeque<>();
        toVisit.add((Resource) root);
        while (!toVisit.isEmpty()) {
            Resource next = toVisit.pop();
            if (!visited.add(next)) {
                continue;
            }
            for (Statement statement : statements.withSubject(next)) {
                if (predicates.contains(statement.getPredicate())) {
                    result.add(statement);
                }
                if (statement.getObject().isResource()) {
                    toVisit.add((Resource) statement.getObject());
                }
            }
        }
        return result;
    }

}
