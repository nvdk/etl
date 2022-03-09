package com.linkedpipes.etl.plugin.configuration.model;

import com.linkedpipes.etl.model.vocabulary.LP;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationDescriptionAdapter {

    /**
     * This function does not load the resources.
     */
    static public ConfigurationDescription asConfigurationDescription(
            Collection<Statement> statements) {
        Resource resource = findDescriptionResource(statements);
        if (resource == null) {
            return null;
        }
        ConfigurationDescription result = new ConfigurationDescription();
        result.resource = resource;
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP.CONFIG_DESC_TYPE:
                    if (value instanceof IRI) {
                        result.configurationType = (IRI) value;
                    }
                    break;
                case LP.CONFIG_DESC_MEMBER:
                    if (value instanceof Resource) {
                        result.members.add(
                                loadMember((Resource) value, statements));
                    }
                    break;
                case LP.CONFIG_DESC_CONTROL:
                    if (value instanceof IRI) {
                        result.globalControlProperty = (IRI) value;
                    }
                    break;
                default:
                    break;
            }

        }

        return result;
    }

    static protected Resource findDescriptionResource(
            Collection<Statement> statements) {
        IRI descriptionType = SimpleValueFactory.getInstance().createIRI(
                LP.CONFIG_DESCRIPTION);
        List<Resource> candidates = statements.stream()
                .filter(st -> st.getPredicate().equals(RDF.TYPE))
                .filter(st -> descriptionType.equals(st.getObject()))
                .map(Statement::getSubject)
                .collect(Collectors.toList());
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        return null;
    }

    static protected ConfigurationDescription.Member loadMember(
            Resource resource, Collection<Statement> statements) {
        ConfigurationDescription.Member result =
                new ConfigurationDescription.Member();
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP.CONFIG_DESC_PROPERTY:
                    if (value instanceof IRI) {
                        result.property = (IRI) value;
                    }
                    break;
                case LP.CONFIG_DESC_CONTROL:
                    if (value instanceof IRI) {
                        result.controlProperty = (IRI) value;
                    }
                    break;
                case LP.IS_PRIVATE:
                    if (value instanceof Literal) {
                        result.isPrivate = (Literal) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public static List<Statement> asStatements(
            ConfigurationDescription definition) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<Statement> result = new ArrayList<>();
        Resource resource = definition.resource;
        result.add(valueFactory.createStatement(
                resource,
                RDF.TYPE,
                valueFactory.createIRI(LP.CONFIG_DESCRIPTION),
                resource
        ));
        result.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(LP.CONFIG_DESC_TYPE),
                definition.configurationType,
                resource
        ));
        if (definition.globalControlProperty != null) {
            result.add(valueFactory.createStatement(
                    resource,
                    valueFactory.createIRI(LP.CONFIG_DESC_CONTROL),
                    definition.globalControlProperty,
                    resource
            ));
        }
        int counter = 0;
        for (ConfigurationDescription.Member member :
                definition.members) {
            Resource memberResource = valueFactory.createIRI(
                    resource.stringValue() + "/"
                            + String.format("%03d", counter++));

            result.add(valueFactory.createStatement(
                    resource,
                    valueFactory.createIRI(LP.HAS_MEMBER),
                    memberResource,
                    resource
            ));

            writeMember(valueFactory, result, memberResource, resource, member);
        }
        return result;
    }

    protected static void writeMember(
            ValueFactory valueFactory, List<Statement> collector,
            Resource resource, Resource graph,
            ConfigurationDescription.Member member
    ) {
        collector.add(valueFactory.createStatement(
                resource,
                RDF.TYPE,
                valueFactory.createIRI(LP.MEMBER),
                graph
        ));
        collector.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(LP.CONFIG_DESC_PROPERTY),
                member.property,
                graph
        ));
        collector.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(LP.CONFIG_DESC_CONTROL),
                member.controlProperty,
                graph
        ));
        if (member.isPrivate != null) {
            collector.add(valueFactory.createStatement(
                    resource,
                    valueFactory.createIRI(LP.IS_PRIVATE),
                    member.isPrivate,
                    graph
            ));
        }
    }

}
