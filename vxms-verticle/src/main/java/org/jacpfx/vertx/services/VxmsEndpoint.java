package org.jacpfx.vertx.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import or.jacpfx.spi.ServiceDiscoverySpi;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.configuration.EndpointConfiguration;
import org.jacpfx.common.util.ConfigurationUtil;

import java.util.Optional;
import java.util.function.Consumer;

import static org.jacpfx.vertx.util.ServiceUtil.*;

/**
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project
 * Created by amo on 28.10.15.
 */
public abstract class VxmsEndpoint extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(VxmsEndpoint.class);

    private Consumer<Future<Void>> onStop;

    @Override
    public final void start(final Future<Void> startFuture) {
        long startTime = System.currentTimeMillis();
        // register info (keepAlive) handler
        vertx.eventBus().consumer(ConfigurationUtil.getServiceName(getConfig(), this.getClass()) + "-info", this::info);
        initEndpoint(startFuture);
        long endTime = System.currentTimeMillis();
        log.info("start time: " + (endTime - startTime) + "ms");
    }


    private void initEndpoint(final Future<Void> startFuture) {
        final int port = ConfigurationUtil.getEndpointPort(getConfig(), this.getClass());
        final String host = ConfigurationUtil.getEndpointHost(getConfig(), this.getClass());
        final String contexRoot = ConfigurationUtil.getContextRoot(getConfig(), this.getClass());
        final CustomServerOptions endpointConfig = ConfigurationUtil.getEndpointOptions(this.getClass());
        final HttpServerOptions options = endpointConfig.getServerOptions(this.getConfig());
        final HttpServer server = vertx.createHttpServer(options.setHost(host).setPort(port));

        log.info("create http server: "+options.getHost()+":"+options.getPort());
        final boolean secure = options.isSsl();
        final boolean contextRootSet = isContextRootSet(Optional.ofNullable(contexRoot).orElse(""));
        final Router topRouter = Router.router(vertx);
        final Router subRouter = contextRootSet ? Router.router(vertx) : null;
        final Router router = contextRootSet ? subRouter : topRouter;
        final EndpointConfiguration endpointConfiguration = getEndpointConfiguration(this);

        getConfig().put("secure", secure);

        initEndoitConfiguration(endpointConfiguration, vertx, router, secure, host, port);
        // check for websocket extension
        Optional.
                ofNullable(getWebSocketSPI()).
                ifPresent(webSockethandlerSPI -> webSockethandlerSPI.registerWebSocketHandler(server, vertx, getConfig(), this));
        // check for REST extension
        Optional.
                ofNullable(getRESTSPI()).
                ifPresent(resthandlerSPI -> resthandlerSPI.initRESTHandler(vertx, router, getConfig(), this));

        postEndoitConfiguration(endpointConfiguration, router);

        if (contextRootSet)
            topRouter.mountSubRouter(getCleanContextRoot(Optional.ofNullable(contexRoot).orElse("")), subRouter);

        server.requestHandler(topRouter::accept).listen(status -> {
            if (status.succeeded()) {
                log("started on PORT: " + port + " host: " + host);
                // check for Service discovery extension
                final ServiceDiscoverySpi serviceDiscovery = getServiceDiscoverySPI();
                if (serviceDiscovery != null) {
                    handleServiceRegistration(serviceDiscovery,startFuture);
                } else {
                    postConstruct(topRouter, startFuture);
                }
                log("startFuture.isComplete(): " + startFuture.isComplete() + " startFuture.failed(): " + startFuture.failed());
            } else {
                startFuture.fail(status.cause());
            }

        });
    }

    private boolean isContextRootSet(String cRoot) {
        return !cRoot.trim().equals(SLASH) && cRoot.length() > 1;
    }

    private void handleServiceRegistration(ServiceDiscoverySpi serviceDiscovery, final Future<Void> startFuture) {
        final AbstractVerticle current = this;
        Optional.ofNullable(serviceDiscovery).ifPresent(sDicovery -> {
            sDicovery.registerService(() -> postConstruct(startFuture), ex -> {
                if (!startFuture.isComplete()) startFuture.fail(ex);
            }, current);
            this.onStop = (stopFuture) -> sDicovery.disconnect();
        });
    }

    /**
     * Stop the service.<p>
     * This is called by Vert.x when the service instance is un-deployed. Don't call it yourself.<p>
     * If your verticle does things in it's shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     * @param stopFuture  a future which should be called when verticle clean-up is complete.
     * @throws Exception
     */
    public final void stop(Future<Void> stopFuture) throws Exception {
        Optional.ofNullable(onStop).ifPresent(stop -> stop.accept(stopFuture));
        if (!stopFuture.isComplete()) stopFuture.complete();
    }




    /**
     * Overwrite this method to handle your own initialisation after all vxms init is done
     *
     * @param router      the http router handler
     * @param startFuture the vert.x start future
     */
    protected void postConstruct(Router router, final Future<Void> startFuture) {
        postConstruct(startFuture);
    }

    /**
     * Overwrite this method to handle your own initialisation after all vxms init is done
     *
     * @param startFuture
     */
    protected void postConstruct(final Future<Void> startFuture) {
        startFuture.complete();
    }


    private void initEndoitConfiguration(EndpointConfiguration endpointConfiguration, Vertx vertx, Router router, boolean secure, String host, int port) {
        Optional.of(endpointConfiguration).ifPresent(endpointConfig -> {

            endpointConfig.corsHandler(router);

            endpointConfig.bodyHandler(router);

            endpointConfig.cookieHandler(router);

            endpointConfig.sessionHandler(vertx, router);

            endpointConfig.customRouteConfiguration(vertx, router, secure, host, port);
        });
    }

    private void postEndoitConfiguration(EndpointConfiguration endpointConfiguration, Router router) {
        Optional.of(endpointConfiguration).ifPresent(endpointConfig -> endpointConfig.staticHandler(router));
    }



    private void log(final String value) {
        log.info(value);
    }


    private void info(Message m) {
        // TODO create info message about service
    }


    private JsonObject getConfig() {
        return context != null ? context.config() : new JsonObject();
    }


}
