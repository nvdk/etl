package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.model.vocabulary.LP;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationDescriptionDefinitionAdapter {

    static public ConfigurationDescriptionDefinition create(
            List<Statement> statements) throws InvalidConfiguration {
        Resource resource = findDescriptionResource(statements);
        ConfigurationDescriptionDefinition result =
                new ConfigurationDescriptionDefinition();
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP.CONFIG_DESC_TYPE:
                    if (value instanceof IRI) {
                        result.forConfigurationType = (IRI) value;
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
            List<Statement> statements) throws InvalidConfiguration {
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
        throw new InvalidConfiguration(
                "Expected one configuration description got {}",
                candidates.size());
    }

    static protected ConfigurationDescriptionDefinition.Member loadMember(
            Resource resource, List<Statement> statements) {
        ConfigurationDescriptionDefinition.Member result =
                new ConfigurationDescriptionDefinition.Member();
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
            ConfigurationDescriptionDefinition definition) {
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
                definition.forConfigurationType,
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
        for (ConfigurationDescriptionDefinition.Member member :
                definition.members) {
            Resource memberResource = valueFactory.createIRI(
                    resource.stringValue() + "/" + ++counter);
            writeMember(valueFactory, result, memberResource, resource, member);
        }
        return result;
    }

    public static void writeMember(
            ValueFactory valueFactory, List<Statement> collector,
            Resource resource, Resource graph,
            ConfigurationDescriptionDefinition.Member member
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
