((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/vocabulary",
      "@client/pipeline/edit/model/pipeline-model"
    ], definition);
  }
})((vocabulary, pipelineModel) => {

  // TODO Move all pipeline related operations to this file. Also from repositories.

  const LP = vocabulary.LP;

  const MIME_TYPE_JSON_LD = "application/ld+json";

  function executePipeline($http, iri, options) {
    const config = createExecutionConfiguration(options);
    return postPipelineExecution($http, iri, config);
  }

  function createExecutionConfiguration(options) {
    const {keepDebugData, debugTo, execution, mapping, resume} = options;
    const configuration = {
      "@id": "",
      "@type": LP.EXEC_OPTIONS,
    };

    if (debugTo) {
      configuration[LP.RUN_TO] = {"@id": debugTo};
    }

    if (keepDebugData) {
      configuration[LP.SAVE_DEBUG] = true;
      configuration[LP.DELETE_WORKING] = false;
    } else {
      configuration[LP.SAVE_DEBUG] = false;
      configuration[LP.DELETE_WORKING] = true;
    }

    if (execution) {
      const executionMapping = {};
      executionMapping[LP.HAS_EXECUTION_ETL] = {"@id": execution};
      if (mapping) {
        executionMapping[LP.MAPPING] = createComponentMapping(mapping);
      }
      if (resume) {
        executionMapping[LP.RESUME] = createComponentMapping(resume);
      }
      configuration[LP.EXECUTION_MAPPING] = executionMapping;
    }

    return configuration;
  }

  function createComponentMapping(mapping) {
    return mapping.map((item) => ({
      [LP.MAPPING_SOURCE]: {"@id": item["source"]},
      [LP.MAPPING_TARGET]: {"@id": item["target"]}
    }));
  }

  function postPipelineExecution($http, iri, config) {
    const url = "./resources/executions?pipeline=" + encodeURIComponent(iri);
    return $http.post(url, config);
  }

  function createEmptyPipeline($http) {
    const url = "./api/v2/pipelines/";
    const pipeline = {
      "@type": "http://linkedpipes.com/ontology/Pipeline",
      "http://www.w3.org/2004/02/skos/core#prefLabel": "New pipeline",
      "http://etl.linkedpipes.com/ontology/version": 5
    };
    return $http.post(url, pipeline, {
      "headers": {
        "Content-Type": MIME_TYPE_JSON_LD,
        "accept": MIME_TYPE_JSON_LD,
      }
    }).then(response => response.headers("location"));
  }

  function copyPipelineFromIri($http, iri) {
    return loadLocal($http, iri)
      .then(pipeline => copyPipelineFromData($http, pipeline));
  }

  function asLocalFromIri($http, iri, updateTemplates) {
    return $http({
      "method": "GET",
      "url": iri,
      "headers": {
        "Accept": MIME_TYPE_JSON_LD,
      },
    }).then(response => response.data)
      .then(pipeline => asLocalPipeline(
        $http, pipeline, "application/ld+json", "pipeline.jsonld",
        updateTemplates)
      );
  }

  function asLocalPipeline(
    $http, pipeline, pipelineMimeType, pipelineFileName, updateTemplates
  ) {
    const formData = new FormData();

    const options = {
      "@type": "http://linkedpipes.com/ontology/ImportOptions",
      "http://etl.linkedpipes.com/ontology/importTemplates": true,
      "http://etl.linkedpipes.com/ontology/updateTemplates": updateTemplates,
    };
    formData.append("option", new Blob([JSON.stringify(options)], {
      "type": MIME_TYPE_JSON_LD
    }), "options.jsonld");

    formData.append("pipeline", new Blob([JSON.stringify(pipeline)], {
      "type": pipelineMimeType
    }), pipelineFileName);

    const url = "./api/v2/pipelines/localize";
    const postConfiguration = {
      // Do not transform data.
      "transformRequest": angular.identity,
      "headers": {
        // By this angular add Content-Type itself.
        "Content-Type": undefined,
        "accept": MIME_TYPE_JSON_LD,
      }
    };
    return $http.post(url, formData, postConfiguration)
      .then((data) => data["data"]);

  }

  function asLocalFromFile($http, formFile, updateTemplates) {
    return loadFromFile(formFile)
      .then(pipeline => asLocalPipeline(
        $http, pipeline, undefined, formFile.name, updateTemplates))
  }

  function loadFromFile(formFile) {
    return new Promise((accept, reject) => {
      const reader = new FileReader();
      reader.addEventListener("load", function (event) {
        const pipeline = JSON.parse(event.target.result);
        accept(pipeline);
      });
      reader.addEventListener("error", function (event) {
        reject();
      });
      reader.readAsBinaryString(formFile);
    });
  }

  function loadLocal($http, iri) {
    return $http({
      "method": "GET",
      "url": "/api/v2/pipelines/definition?iri=" + encodeURIComponent(iri),
      "headers": {
        "Accept": MIME_TYPE_JSON_LD,
      },
    }).then(response => response.data);
  }

  function deletePipeline($http, iri) {
    return $http({
      "method": "DELETE",
      "url": "/api/v2/pipelines/?iri=" + encodeURIComponent(iri),
    });
  }

  function savePipeline($http, jsonld) {
    return $http({
      "method": "POST",
      "url": "/api/v2/pipelines/",
      "headers": {
        "Content-Type": MIME_TYPE_JSON_LD,
        "Accept": MIME_TYPE_JSON_LD,
      },
      "data": jsonld
    });
  }

  function copyPipelineFromData($http, pipelineJsonLd) {
    const model = pipelineModel.createFromJsonLd(pipelineJsonLd);
    pipelineModel.setPipelineResource(model, "_:");
    const newLabel = "Copy of " + pipelineModel.getPipelineLabel(model)
    pipelineModel.setPipelineLabel(model, newLabel);
    const blankNodePipeline = pipelineModel.asJsonLd(model);

    return savePipeline($http, blankNodePipeline)
      .then(response => response.headers("location"));
  }

  function downloadFullPipeline($http, iri) {
    const url = "/api/v2/pipelines/full?iri=" + encodeURIComponent(iri);
    return $http({
      "method": "GET",
      "url": url,
      "headers": {
        "Accept": MIME_TYPE_JSON_LD,
      },
    });
  }

  function downloadFullPipelinePublic($http, iri) {
    const url = "/api/v2/pipelines/full-public?iri=" + encodeURIComponent(iri);
    return $http({
      "method": "GET",
      "url": url,
      "headers": {
        "Accept": MIME_TYPE_JSON_LD,
      },
    });
  }

  function importFromIri($http, iri, updateTemplates) {
    return $http({
      "method": "GET",
      "url": iri,
      "headers": {
        "Accept": MIME_TYPE_JSON_LD,
      },
    }).then(response => response.data)
      .then(pipeline => importPipeline(
        $http, pipeline, "application/ld+json", "pipeline.jsonld",
        updateTemplates)
      );
  }

  function importPipeline(
    $http, pipeline, pipelineMimeType, pipelineFileName, updateTemplates
  ) {
    // TODO Merge with localize pipeline
    const formData = new FormData();

    const options = {
      "@type": "http://linkedpipes.com/ontology/ImportOptions",
      "http://etl.linkedpipes.com/ontology/importTemplates": true,
      "http://etl.linkedpipes.com/ontology/updateTemplates": updateTemplates,
    };
    formData.append("option", new Blob([JSON.stringify(options)], {
      "type": MIME_TYPE_JSON_LD
    }), "options.jsonld");

    formData.append("pipeline", new Blob([JSON.stringify(pipeline)], {
      "type": pipelineMimeType
    }), pipelineFileName);

    const url = "./api/v2/pipelines/import";
    const postConfiguration = {
      // Do not transform data.
      "transformRequest": angular.identity,
      "headers": {
        // By this angular add Content-Type itself.
        "Content-Type": undefined,
        "accept": MIME_TYPE_JSON_LD,
      }
    };
    return $http.post(url, formData, postConfiguration)
      .then(response => response.data)
      // TODO We should actually load the pipelines here, it use pipeline list.
      .then(jsonld => jsonld[0]["@id"]);
  }

  function importFromFile($http, formFile, updateTemplates) {
    return loadFromFile(formFile)
      .then(pipeline => importPipeline(
        $http, pipeline, undefined, formFile.name, updateTemplates))
  }

  return {
    "executePipeline": executePipeline,
    "createEmptyPipeline": createEmptyPipeline,
    "copyPipelineFromIri": copyPipelineFromIri,
    "asLocalFromIri": asLocalFromIri,
    "asLocalFromFile": asLocalFromFile,
    "loadLocal": loadLocal,
    "deletePipeline": deletePipeline,
    "savePipeline": savePipeline,
    "copyPipelineFromData": copyPipelineFromData,
    "downloadFullPipeline": downloadFullPipeline,
    "downloadFullPipelinePublic": downloadFullPipelinePublic,
    "importFromIri": importFromIri,
    "importFromFile": importFromFile,
  }

});
