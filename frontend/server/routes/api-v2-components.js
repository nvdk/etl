"use strict";

const router = require("express").Router();

const {proxyGet, proxyPost, proxyDelete} = require("./request-utilities");
const configuration = require("../configuration");

module.exports = router;

const storageUrl = configuration.storageUrl+ "/api/v2/";

router.get("/", (req, res) => {
  const options = {
    "url": storageUrl + "template/list",
    "headers": req.headers,
  };
  proxyGet(res, options);
});

router.get("/definition", (req, res) => {
  const url = storageUrl + "template/definition?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/configuration-description", (req, res) => {
  const url = storageUrl + "template/configuration-description?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/stored-configuration", (req, res) => {
  const url = storageUrl + "template/stored-configuration?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/new-configuration", (req, res) => {
  const url = storageUrl + "template/new-configuration?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/effective-configuration", (req, res) => {
  const url = storageUrl + "template/effective-configuration?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/file", (req, res) => {
  const url = storageUrl + "template/file?" +
    "iri=" + encodeURIComponent(req.query.iri) +
    "&path=" + encodeURIComponent(req.query.path);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.get("/template-usage", (req, res) => {
  const url = storageUrl + "assistant/template-usage?" +
    "iri=" + encodeURIComponent(req.query.iri);
  proxyGet(res, {"url": url, "headers": req.headers});
});

router.post("/", (req, res) => {
  const url = storageUrl + "template";
  proxyPost(req, url, res);
});

router.delete("/", (req, res) => {
  const url = storageUrl + "template?iri=" + encodeURIComponent(req.query.iri);
  proxyDelete(req, url, res);
});
