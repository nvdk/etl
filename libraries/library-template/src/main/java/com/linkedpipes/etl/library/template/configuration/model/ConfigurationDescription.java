package com.linkedpipes.etl.library.template.configuration.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

import java.util.Map;
import java.util.Set;

/**
 * Configuration description for a single entity type.
 */
public record ConfigurationDescription(
        /*
         * Resource of description entry.
         */
        Resource resource,
        /*
         * Type of configuration this description is designated for.
         */
        IRI configurationType,
        /*
         * If provided the value is used for all controlProperties,
         * i.e. effectively act as controlProperty but for the whole
         * configuration instance.
         */
        IRI globalControlProperty,
        /*
         * Control information for properties.
         */
        Map<IRI, Member> members
) {

    public record Member(
            /*
             * IRI of predicate used to store the value.
             */
            IRI property,
            /*
             * IRI of predicate with control for this member.
             */
            IRI control,
            /*
             * IRI of predicate with substitution.
             */
            IRI substitution,
            /*
             * Represent private (non-public) configuration.
             */
            boolean isPrivate,
            /*
             * Predicates that are private within the nested entities
             * (subtree) of this member, the entities themselves
             * and their other predicates stay public.
             */
            Set<IRI> privateProperties
    ) {

        public Member {
            privateProperties = privateProperties == null
                    ? Set.of() : Set.copyOf(privateProperties);
        }

    }

}
