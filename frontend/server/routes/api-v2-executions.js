"use strict";

const request = require("request");
const router = require("express").Router();

const {handleConnectionError, proxyGet} = require("./request-utilities");
const configuration = require("../configuration");

module.exports = router;

const storageUrl = configuration.storageUrl + "/api/v2/";

// router.get("/debug/metadata/**", (req, res) => {
//   let url = monitorUrl + req.originalUrl.replace("/api/v1/", "");
//   proxyGet(res, url);
// });
//
// router.get("/debug/data/**", (req, res) => {
//   let url = monitorUrl + req.originalUrl.replace("/api/v1/", "");
//   proxyGet(res, url);
// });