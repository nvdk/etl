const request = require("request");

const proxyGet = (res, options) => {
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .on("response", withCors)
    .pipe(res);
}

const handleConnectionError = (res, error) => {
  console.error("Connection failed:\n", error);
  res.status(503).json({
    "error": {
      "type": "connection",
      "source": "frontend"
    }
  });
}

const withCors = (res) => {
  res.headers["Access-Control-Allow-Origin"] = "*";
}

const proxyPost = (req, url, res) => {
  const proxyRequest = request.post(
    url, {"headers": req.headers, "form": req.body});

  req.pipe(proxyRequest, {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}

const proxyDelete = (req, url, res) => {
  const proxyRequest = request.del(url, {"headers": req.headers});
  proxyRequest
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}


module.exports = {
  proxyGet,
  handleConnectionError,
  withCors,
  proxyPost,
  proxyDelete,
};
