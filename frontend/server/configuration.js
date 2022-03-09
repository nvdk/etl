"use strict";

const propertiesReader = require("properties-reader");

// Load configuration from environment.
const configuration = {
  "port": process.env["LP_ETL_FRONTEND_PORT"],
  "instanceLabel": process.env["LP_ETL_FRONTEND_LABEL"] ?? "LinkedPipes ETL",
  "domain": process.env["LP_ETL_DOMAIN"],
  "storageUrl": process.env["LP_ETL_STORAGE_URL"],
  "executorMonitorUrl": process.env["LP_ETL_MONITOR_URL"],
};

// Load configuration from a configuration file.
if (process.env.configFileLocation !== undefined) {
  process.env.configFileLocation = "../deploy/configuration.properties";
  console.log("Loading configuration from: ", process.env.configFileLocation);
  const properties = propertiesReader(process.env.configFileLocation);
  configuration["port"] =
    properties.get("frontend.webserver.port")
    ?? configuration["port"];
  configuration["instanceLabel"] =
    properties.get("frontend.instance-label")
    ?? configuration["instanceLabel"];
  configuration["domain"] =
    properties.get("domain.uri")
    ?? configuration["domain"];
  configuration["storageUrl"] =
    properties.get("domain.uri")
    ?? configuration["storageUrl"];
  configuration["executorMonitorUrl"] =
    properties.get("executor-monitor.webserver.uri")
    ?? configuration["executorMonitorUrl"];
}

module.exports = configuration;
