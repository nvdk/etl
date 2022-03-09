package com.linkedpipes.etl.storage.http.server;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimingHttpChannelListener implements HttpChannel.Listener {

    private static final Logger LOG =
            LoggerFactory.getLogger(TimingHttpChannelListener.class);

    private final ConcurrentMap<Request, Long> times =
            new ConcurrentHashMap<>();

    @Override
    public void onRequestBegin(Request request) {
        times.put(request, System.currentTimeMillis());
    }

    @Override
    public void onRequestEnd(Request request) {
        long begin = times.remove(request);
        long elapsed = System.currentTimeMillis() - begin;
        LOG.info("Request {} : {} took {} ms",
                request.getMethod(), request.getHttpURI(), elapsed);
    }

}
