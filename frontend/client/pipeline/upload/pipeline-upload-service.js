((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../../app-service/vocabulary",
      "../pipeline-api",
    ], definition);
  }
})((vocabulary, pipelines) => {

  function factory($http, $timeout, $location, $status, $templates) {

    let $scope;

    function initialize(scope) {
      $scope = scope;
      //
      $scope.fileReady = false;
      $scope.uploading = false;
      $scope.type = "file";
      $scope.updateTemplates = false;
    }

    function onWatchFile() {
      if (!$scope.file) {
        $scope.fileReady = false;
      } else if ($scope.file.$error) {
        $scope.fileReady = false;
      } else {
        $scope.fileReady = true;
      }
    }

    function onUpload() {
      $scope.uploading = true;
      if ($scope.type === "file") {
        if (!$scope.fileReady) {
          return;
        }
        importFile();
      } else if ($scope.type === "url") {
        importUrl();
      } else {
        console.log("Unknown import type:", $scope.type);
      }
    }

    function importFile() {
      pipelines.importFromFile($http, $scope.file, $scope.updateTemplates)
        .then((iri) => {
          reloadTemplates()
            .catch((err) => $status.httpError("Can't update templates.", err))
            .then(() => redirectToPipeline(iri));
        })
        .catch(error => $status.httpError("Can't copy pipeline.", error));
    }

    function reloadTemplates() {
      return $templates.forceLoad();
    }

    function redirectToPipeline(iri) {
      $location.path("/pipelines/edit/canvas").search({"pipeline": iri});
    }

    function importUrl() {
      pipelines.importFromIri($http, $scope.url, $scope.updateTemplates)
        .then((iri) => {
          reloadTemplates()
            .catch((err) => $status.httpError("Can't update templates.", err))
            .then(() => redirectToPipeline(iri));
        })
        .catch(error => $status.httpError("Can't copy pipeline.", error));
    }

    return {
      "initialize": initialize,
      "onUpload": onUpload,
      "onWatchFile": onWatchFile
    };
  }

  factory.$inject = [
    "$http",
    "$timeout",
    "$location",
    "status",
    "template.service"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    app.factory("pipeline.upload.service", factory);
  }

});