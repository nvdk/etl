package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationDescriptionDefinition {

    public static class Member {

        public IRI property;

        public IRI controlProperty;

        public Literal isPrivate;

    }

    public Resource resource;

    public IRI forConfigurationType;

    /**
     * If provided the value is used for all controlProperties, i.e. effectively
     * act as controlProperty but for the whole configuration instance.
     */
    public IRI globalControlProperty;

    public List<Member> members = new ArrayList<>();

}
