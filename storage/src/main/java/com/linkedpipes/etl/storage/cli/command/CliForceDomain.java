package com.linkedpipes.etl.storage.cli.command;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.storage.cli.StorageConfiguration;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.cli.CliCommand;
import com.linkedpipes.etl.storage.cli.CliCommandParser;
import com.linkedpipes.etl.storage.importer.AlignPipelineResources;
import com.linkedpipes.etl.storage.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.storage.rdf.Statements;
import com.linkedpipes.etl.storage.rdf.UpdateResources;
import com.linkedpipes.etl.storage.store.Store;
import com.linkedpipes.etl.storage.store.StoreFactory;
import com.linkedpipes.etl.storage.template.reference.model.ReferenceTemplate;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Load all data from a store and change the domain to match with
 * the one in the configuration. This action apply to
 */
public class CliForceDomain implements CliCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CliCommand.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private StorageConfiguration configuration;

    @Override
    public CliCommandParser getCliCommandParser() {
        Options options = new Options();
        options.addOption("y", "Skip command line prompt.");
        return new CliCommandParser(
                "force-domain",
                "Make sure all data in this instance use domain from " +
                        "configuration.",
                options);
    }

    @Override
    public int execute(StorageConfiguration configuration, String[] args) {
        this.configuration = configuration;
        if (!isUserSure(args)) {
            return 0;
        }
        Store store;
        try {
            store = createStore();
        } catch (StorageException ex) {
            LOG.error("Can't create store.", ex);
            return 1;
        }
        try {
            Map<Resource, Resource> templatesMapping =
                    buildTemplateMapping(store);
            updateReferenceTemplates(store, templatesMapping);
            updatePipelines(store, templatesMapping);
        } catch (StorageException ex) {
            LOG.error("Can't update resources.", ex);
            return 1;
        }
        return 0;
    }

    private boolean isUserSure(String[] args) {
        CliCommandParser parser = getCliCommandParser();
        CommandLine cmd = parser.parse(args);
        if (cmd == null) {
            return false;
        }
        if (cmd.hasOption("-y")) {
            return true;
        }
        System.out.println(
                "Create a backup before running this operation.");
        System.out.print(
                "As it can beak pipelines and components in case of a failure.");
        System.out.print("Type \"yes\" and press enter to continue: ");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        return line.contains("yes");
    }

    private Store createStore()
            throws StorageException {
        StoreFactory storeFactory = new StoreFactory(
                configuration.storeDirectory(), configuration.baseUrl());
        return storeFactory.createStore();
    }

    private Map<Resource, Resource> buildTemplateMapping(Store store)
            throws StorageException {
        Map<Resource, Resource> result = new HashMap<>();
        for (String iri : store.listReferencesTemplate()) {
            if (isLocal(iri)) {
                continue;
            }
            String targetIri = store.reserveReferenceIri();
            result.put(
                    valueFactory.createIRI(iri),
                    valueFactory.createIRI(targetIri));
        }
        return result;
    }

    private boolean isLocal(String iri) {
        return iri.startsWith(configuration.baseUrl());
    }

    private void updateReferenceTemplates(
            Store store, Map<Resource, Resource> mapping)
            throws StorageException {
        for (String iri : store.listReferencesTemplate()) {
            Optional<ReferenceTemplate> oldTemplateWrap =
                    store.loadReferenceTemplate(iri);
            if (oldTemplateWrap.isEmpty()) {
                LOG.warn("Can't get reference template:\n  {}", iri);
                continue;
            }
            ReferenceTemplate oldTemplate =  oldTemplateWrap.get();
            if (!mapping.containsKey(oldTemplate.resource())
                    && !mapping.containsKey(oldTemplate.template())) {
                return;
            }
            ReferenceTemplate newTemplate =
                    updateTemplate(mapping, oldTemplate);
            store.storeReferenceTemplate(newTemplate);
            store.deleteReferenceTemplate(iri);
            LOG.info("Updated reference template:\n  {}\n  {}",
                    iri, newTemplate.resource());
        }
    }

    private ReferenceTemplate updateTemplate(
            Map<Resource, Resource> mapping, ReferenceTemplate template) {
        Resource resource = mapping.getOrDefault(
                template.resource(), template.resource());
        Resource parent = mapping.getOrDefault(
                template.template(), template.template());
        Resource configurationGraph = template.configurationGraph();
        Statements configuration = template.configuration();
        if (resource != template.resource()) {
            // There is change in resource, we need to update configuration.
            configurationGraph =
                    ReferenceTemplate.defaultConfigurationGraph(resource);
            configuration = UpdateResources.apply(
                    configurationGraph.stringValue(), configuration);
        }
        return new ReferenceTemplate(
                resource, template.template(), template.prefLabel(),
                template.description(), template.note(), template.color(),
                template.tags(), template.knownAs(),
                parent,
                template.version(),
                configuration,
                configurationGraph);
    }

    private void updatePipelines(
            Store store, Map<Resource, Resource> mapping)
            throws StorageException {
        for (String iri : store.listPipelines()) {
            Optional<Pipeline> oldPipelineWrap = store.loadPipeline(iri);
            if (oldPipelineWrap.isEmpty()) {
                LOG.warn("Can't get pipeline:\n  {}", iri);
                continue;
            }
            Pipeline oldPipeline = oldPipelineWrap.get();
            Pipeline newPipeline = oldPipeline;
            newPipeline = updatePipeline(store, newPipeline);
            newPipeline = updatePipelineTemplates(mapping, newPipeline);
            if (oldPipeline == newPipeline) {
                continue;
            }
            store.storePipeline(newPipeline);
            if (Objects.equal(oldPipeline.resource(), newPipeline.resource())) {
                LOG.info("Updated pipeline:\n  {}", iri);
                continue;
            }
            LOG.info("Updated pipeline:\n  {}\n  {}",
                    iri, newPipeline.resource());
            store.deletePipeline(iri);
        }
    }

    private Pipeline updatePipeline(Store store, Pipeline pipeline)
            throws StorageException {
        if (isLocal(pipeline.resource().stringValue())) {
            return pipeline;
        }
        Resource resource = valueFactory.createIRI(
                store.reservePipelineIri(
                        getPipelineSuffix(pipeline.resource())));
        return AlignPipelineResources.apply(new Pipeline(
                resource, pipeline.label(), pipeline.version(),
                pipeline.note(), pipeline.tags(), pipeline.executionProfile(),
                pipeline.components(), pipeline.connections()));
    }

    private String getPipelineSuffix(Resource resource) {
        if (!resource.isResource()) {
            return null;
        }
        String iri = resource.stringValue();
        return iri.substring(iri.lastIndexOf("/") + 1);
    }

    private Pipeline updatePipelineTemplates(
            Map<Resource, Resource> mapping, Pipeline pipeline) {
        List<PipelineComponent> components = new ArrayList<>();
        boolean componentsHaveChanged = false;
        for (PipelineComponent component : pipeline.components()) {
            if (mapping.containsKey(component.template())) {
                components.add(new PipelineComponent(
                        component.resource(), component.label(),
                        component.description(), component.note(),
                        component.color(), component.xPosition(),
                        component.yPosition(),
                        mapping.get(component.template()),
                        component.disabled(), component.configurationGraph(),
                        component.configuration()
                ));
                componentsHaveChanged = true;
            } else {
                components.add(component);
            }
        }
        if (componentsHaveChanged) {
            return new Pipeline(
                    pipeline.resource(), pipeline.label(), pipeline.version(),
                    pipeline.note(), pipeline.tags(),
                    pipeline.executionProfile(), components,
                    pipeline.connections());
        }
        return pipeline;
    }

}
