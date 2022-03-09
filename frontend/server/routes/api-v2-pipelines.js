"use strict";

const router = require("express").Router();

const {proxyGet, proxyPost, proxyDelete} = require("./request-utilities");
const configuration = require("../configuration");

module.exports = router;

const storageUrl = configuration.storageUrl + "/api/v2/";

router.get("/", (req, res) => {
  const options = {
    "url": storageUrl + "pipeline/list",
    "headers": req.headers,
  };
  proxyGet(res, options);
});

router.get("/definition", (req, res) => {
  const url = storageUrl + "pipeline/definition?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/definition-public", (req, res) => {
  const url = storageUrl + "pipeline/definition?removePrivateConfiguration&" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/full", (req, res) => {
  const url = storageUrl + "pipeline/full?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/full-public", (req, res) => {
  const url = storageUrl + "pipeline/full?removePrivateConfiguration&" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/squashed", (req, res) => {
  const url = storageUrl + "pipeline/squashed?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/squashed-public", (req, res) => {
  const url = storageUrl + "pipeline/squashed?removePrivateConfiguration&" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/assistant", (req, res) => {
  const url = storageUrl + "assistant/pipeline-statistics";
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.post("/localize", (req, res) => {
  const url = storageUrl + "pipeline/localize";
  proxyPost(req, url, res);
});

router.post("/import", (req, res) => {
  const url = storageUrl + "pipeline/import";
  proxyPost(req, url, res);
});

router.post("/", (req, res) => {
  const url = storageUrl + "pipeline";
  proxyPost(req, url, res);
});

router.delete("/", (req, res) => {
  const url = storageUrl + "pipeline?iri=" + encodeURIComponent(req.query.iri);
  proxyDelete(req, url, res);
});
