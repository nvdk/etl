define([
  "jquery",
  "@client/app-service/jsonld/jsonld",
  "../../../template/detail/directive/template-detail-directive",
  "../../../template/template-service"
], function (jQuery, jsonld, detailDirective, templateService) {
  "use strict";

  const LP = {
    "template": "http://linkedpipes.com/ontology/template",
    "color": "http://linkedpipes.com/ontology/color",
    "configurationGraph": "http://linkedpipes.com/ontology/configurationGraph",
    "control": "http://plugins.linkedpipes.com/ontology/configuration/control",
    "version": "http://etl.linkedpipes.com/ontology/version",
  };

  const SKOS = {
    "prefLabel": "http://www.w3.org/2004/02/skos/core#prefLabel",
    "note": "http://www.w3.org/2004/02/skos/core#note"
  };

  const DCTERMS = {
    "description": "http://purl.org/dc/terms/description"
  };

  function controller(
    $scope, $http, $mdDialog, $templates, $status,
    component, componentTemplate, configuration) {

    $scope.api = {};

    $scope.templateToEdit = undefined;
    $scope.configuration = undefined;

    $scope.onSave = () => {
      // Update shared data.
      $scope.api.save();

      const promise = createTemplate(
        $http, $scope.templateToEdit, componentTemplate,
        $scope.configuration, $templates)
        .then((response) => {
          $status.success("Template created.");
          return response;
        })
        .catch((error) => $status.error("Can't create template.", error));
      $mdDialog.hide(promise);
    };

    $scope.onCancel = function () {
      $mdDialog.cancel();
    };

    function initialize() {
      $scope.templateToEdit = {
        "id": "",
        "label": jsonld.r.getPlainString(component, SKOS.prefLabel),
        "description": jsonld.r.getPlainString(
          component, DCTERMS.description),
        "color": jsonld.r.getPlainString(component, LP.color),
        "note": jsonld.r.getPlainString(component, SKOS.note)
      };

      $scope.infoLink = componentTemplate._coreReference.infoLink;

      // Construct a new temporary template object.
      $scope.component = jQuery.extend({}, componentTemplate,
        {
          "id": componentTemplate.id,
          "template": componentTemplate.id,
        });

      $scope.configuration = jQuery.extend(true, [], configuration);
      if ($scope.configuration === undefined) {
        $templates.fetchNewConfig(componentTemplate.id).then((config) => {
          $scope.configuration = jQuery.extend(true, [], config);
          initializeDirective();
        });
      } else {
        initializeDirective();
      }

    }

    /**
     * Must be called once all data are ready. It will initialize the
     * directive.
     */
    function initializeDirective() {
      // The HierarchyTab list all parents of given template, but
      // as the new template is not yet created we can list only from
      // grandparents. A solution is to create a stub template.
      const parents = [];
      Array.prototype.push.apply(parents, $scope.component._parents);
      parents.push($scope.component);

      // We use shallow copy here as we know we will not modify it.
      const template = jQuery.extend(false, [], $scope.component);
      template._parents = parents;

      // $scope.component

      $scope.api.store = {
        "template": template,
        "templateToEdit": $scope.templateToEdit,
        "configuration": $scope.configuration
      };

      if ($scope.api.load !== undefined) {
        $scope.api.load();
      }
    }

    $templates.load().then(initialize);
  }

  function createTemplate(
    $http, newTemplate, newTemplateParent, configuration, $templates) {

    const resource = {
      "@type": ["http://linkedpipes.com/ontology/Template"]
    };
    jsonld.r.setStrings(resource, SKOS.prefLabel, newTemplate.label);
    jsonld.r.setStrings(resource, DCTERMS.description, newTemplate.description);
    jsonld.r.setIRIs(resource, LP.template, newTemplateParent.id);
    if (newTemplate.color !== undefined) {
      jsonld.r.setStrings(resource, LP.color, newTemplate.color);
    }
    jsonld.r.setStrings(resource, SKOS.note, newTemplate.note);
    jsonld.r.setIntegers(resource, LP.version, 5);
    jsonld.r.setIRIs(resource, LP.configurationGraph, "_:configuration");

    const definition = {
      "@graph": [
        {"@graph": resource},
        {
          "@graph": configuration,
          "@id": "_:configuration"
        },
      ],
    };

    return $templates.createTemplate(definition)
      .then(iri => ({
        "template": iri,
        "configuration": configuration,
      }));

  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    detailDirective(app);
    templateService(app);
    app.controller("template.detail.dialog", [
      "$scope", "$http", "$mdDialog",
      "template.service", "status",
      "component", "component-template", "configuration",
      controller]);
  };

});
