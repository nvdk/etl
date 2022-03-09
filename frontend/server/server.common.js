const logger = require("./logging");
const configuration = require("./configuration");

function initialize(app) {
  app.use("/api/v2/", require("./routes/api-v2"));
  app.use("/api/v2/components", require("./routes/api-v2-components"));
  app.use("/api/v2/executions", require("./routes/api-v2-executions"));
  app.use("/api/v2/pipelines", require("./routes/api-v2-pipelines"));

  app.use("/resources/components", require("./routes/resources-components"));
  app.use("/resources/executions", require("./routes/resources-executions"));
  app.use("/resources/pipelines", require("./routes/resources-pipelines"));
}

function start(app) {
  const port = configuration.port;
  app.listen(port, (error) => {
    if (error) {
      logger.error("Can't start server: ", error);
    }
    logger.info("Listening on port: ", port);
  });
}

module.exports = {
  "initialize": initialize,
  "start": start,
};
