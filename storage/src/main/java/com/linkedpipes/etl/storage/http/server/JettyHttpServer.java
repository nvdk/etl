package com.linkedpipes.etl.storage.http.server;

import com.linkedpipes.etl.storage.http.HttpServerException;
import com.linkedpipes.etl.storage.http.StorageHttpApplication;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Documentation for Jetty:
 * https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide/index.html
 */
public class JettyHttpServer {

    private static final Logger LOG =
            LoggerFactory.getLogger(JettyHttpServer.class);

    private final QueuedThreadPool threadPool = new QueuedThreadPool();

    private final Server server = new Server(threadPool);

    private final StorageHttpApplication application;

    public JettyHttpServer(StorageHttpApplication application) {
        this.application = application;
    }

    public void startServer(int port) throws HttpServerException {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.addBean(new TimingHttpChannelListener());
        server.setConnectors(new Connector[]{connector});

        threadPool.setMinThreads(2);
        threadPool.setMaxThreads(8);
        threadPool.setName("jetty");

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(createServlet(), "/api/v2/*");
        server.setHandler(handler);

        try {
            server.start();
        } catch (Exception ex) {
            throw new HttpServerException("Can't start server.", ex);
        }
        LOG.info("HTTP server is running and listening at port: {}", port);
    }

    private ServletHolder createServlet() {
        ServletContainer servlet = new ServletContainer(application);
        return new ServletHolder("linkedpipes-store-servlet", servlet);
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception ex) {
            LOG.error("Can't stop HTTP server.", ex);
        }
    }

}
