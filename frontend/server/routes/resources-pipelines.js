"use strict";

const router = require("express").Router();

const {proxyGet, proxyPost, proxyDelete} = require("./request-utilities");
const configuration = require("../configuration");

module.exports = router;

const storageUrl = configuration.storageUrl + "/api/v2/";

router.get("", (req, res) => {
  const options = {
    "url": storageUrl + "pipeline/list",
    "headers": req.headers,
  };
  proxyGet(res, options);
});

router.get("/:id", (req, res) => {
  const pipeline = configuration.domain + req.originalUrl;
  const options = {
    "url": storageUrl + "pipeline/definition?iri=" + encodeURI(pipeline),
    "headers": req.headers,
  };
  proxyGet(res, options);
});

// router.post("", (req, res) => {
//   const url = storageUrl + "/pipeline";
//   proxyPost(req, url, res);
// });

// router.delete("/:id", (req, res) => {
//   const pipeline = configuration.storage.domain + req.originalUrl;
//   const url = storageUrl + "pipeline?iri=" + encodeURI(pipeline);
//   proxyDelete(req, url, res);
// });
