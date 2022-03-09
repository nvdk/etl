"use strict";

const router = require("express").Router();

const {proxyGet, proxyPost, proxyDelete} = require("./request-utilities");
const configuration = require("../configuration");

module.exports = router;

const storageUrl = configuration.storageUrl + "/api/v2/";

router.get("", (req, res) => {
  const options = {
    "url": storageUrl + "template/list",
    "headers": req.headers,
  };
  proxyGet(res, options);
});

router.get("/:id", (req, res) => {
  const template = configuration.domain + req.originalUrl;
  const options = {
    "url": storageUrl + "template/definition?iri=" + encodeURI(template),
    "headers": req.headers,
  };
  proxyGet(res, options);
});

// router.post("", (req, res) => {
//   const url = storageUrl + "/";
//   proxyPost(req, url, res);
// });

// router.delete("/:id", (req, res) => {
//   const template = configuration.storage.domain + req.originalUrl;
//   const url = storageUrl + "template?iri=" + encodeURI(template);
//   proxyDelete(req, url, res);
// });
