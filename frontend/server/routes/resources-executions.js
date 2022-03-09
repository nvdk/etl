"use strict";

const request = require("request"); // https://github.com/request/request
const executionFactory = require("../execution-factory");

const config = require("../configuration");
const errors = require("../error-codes");

const router = require("express").Router();
module.exports = router;

const monitorApiUrl = config.executorMonitorUrl + "executions";

router.get("", (req, res) => {
  let uri = monitorApiUrl + "/?";
  if (req.query["changedSince"]) {
    uri += "changedSince=" + req.query["changedSince"];
  }
  pipeGet(uri, req.headers, res);
});

function pipeGet(uri, headers, res) {
  const options = {
    "url": uri,
    "headers": updateHeaders(headers),
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}

function updateHeaders(headers) {
  // We use json-ld as default to make it easy to see pipelines from
  // browsers.
  if (headers.accept === "*/*") {
    return {
      ...headers,
      "accept": "application/ld+json"
    }
  }
  return headers;
}

function handleConnectionError(res, error) {
  console.error("Request failed:\n", error);
  res.status(503).json({
    "error": {
      "type": errors.CONNECTION,
      "source": "FRONTEND"
    }
  });
}

router.get("/:id", (req, res) => {
  const uri = monitorApiUrl + "/" + req.params["id"];
  pipeGet(uri, req.headers, res);
});

router.post("/:id/cancel", (req, res) => {
  const url = monitorApiUrl + "/" + req.params["id"] + "/cancel";
  req.pipe(request.post(url))
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.delete("/:id", (req, res) => {
  const uri = monitorApiUrl + "/" + req.params["id"];
  request.del(uri)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/:id/pipeline", (req, res) => {
  const uri = monitorApiUrl + "/" + req.params["id"] + "/pipeline";
  pipeGet(uri, req.headers, res);
});

router.get("/:id/overview", (req, res) => {
  const uri = monitorApiUrl + "/" + req.params["id"] + "/overview";
  pipeGet(uri, req.headers, res);
});

router.get("/:id/logs", (req, res) => {
  const uri = monitorApiUrl + "/" + req.params["id"] + "/logs";
  pipeGet(uri, req.headers, res);
});

router.get("/:id/logs-tail", (req, res) => {
  let uri = monitorApiUrl + "/" + req.params["id"] + "/logs-tail";
  if (req.query["n"] !== undefined) {
    uri += "?n=" + req.query["n"];
  }
  pipeGet(uri, req.headers, res);
});

router.get("/:id/messages/component", (req, res) => {
  const uri =
    monitorApiUrl + "/" + req.params["id"] +
    "/messages/component?iri=" + encodeURIComponent(req.query["iri"]);
  pipeGet(uri, req.headers, res);
});

// router.post('', (req, res) => {
//   executionFactory.create(req, res);
// });
