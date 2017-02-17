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
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project. This class can be extended to create a vxms service
 * Created by Andy Moncsek
 */
public abstract class VxmsEndpoint extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(VxmsEndpoint.class);

    private Consumer<Future<Void>> onStop;

    @Override
    public final void start(final Future<Void> startFuture) {
        // register info (keepAlive) handler
        vertx.eventBus().consumer(ConfigurationUtil.getServiceName(getConfig(), this.getClass()) + "-info", this::info);
        initEndpoint(startFuture);
    }


    /**
     * initiate Endpoint  and all Plugins
     * @param startFuture, the Vertx start feature
     */
    private void initEndpoint(final Future<Void> startFuture) {
        final Class<? extends VxmsEndpoint> serviceClass = this.getClass();
        final int port = ConfigurationUtil.getEndpointPort(getConfig(), serviceClass);
        final String host = ConfigurationUtil.getEndpointHost(getConfig(), serviceClass);
        final String contexRoot = ConfigurationUtil.getContextRoot(getConfig(), serviceClass);
        final CustomServerOptions endpointConfig = ConfigurationUtil.getEndpointOptions(serviceClass);
        final HttpServerOptions options = endpointConfig.getServerOptions(this.getConfig());
        final HttpServer server = vertx.createHttpServer(options.setHost(host).setPort(port));

        log("create http server: "+options.getHost()+":"+options.getPort());
        final boolean secure = options.isSsl();
        final boolean contextRootSet = isContextRootSet(Optional.ofNullable(contexRoot).orElse(""));
        final Router topRouter = Router.router(vertx);
        final Router subRouter = contextRootSet ? Router.router(vertx) : null;
        final Router router = contextRootSet ? subRouter : topRouter;
        final EndpointConfiguration endpointConfiguration = getEndpointConfiguration(this);

        getConfig().put("secure", secure);

        initEndoitConfiguration(endpointConfiguration, vertx, router, secure, host, port);
        initWebSocketExtensions(server);
        initRESTExtensions(router);

        postEndoitConfiguration(endpointConfiguration, router);

        if (contextRootSet)
            topRouter.mountSubRouter(getCleanContextRoot(Optional.ofNullable(contexRoot).orElse("")), subRouter);

        initHTTPEndpoint(startFuture, port, host, server, topRouter);
    }

    /**
     * starts the HTTP Endpoint
     * @param startFuture the vertx start future
     * @param port the port to listen
     * @param host the host to bind
     * @param server the vertx server
     * @param topRouter the router object
     */
    private void initHTTPEndpoint(Future<Void> startFuture, int port, String host, HttpServer server, Router topRouter) {
        server.requestHandler(topRouter::accept).listen(status -> {
            if (status.succeeded()) {
                log("started on PORT: " + port + " host: " + host);
                // check for Service discovery extension
                final ServiceDiscoverySpi serviceDiscovery = getServiceDiscoverySPI();
                if (serviceDiscovery != null) {
                    initServiceDiscovery(serviceDiscovery,startFuture);
                } else {
                    postConstruct(topRouter, startFuture);
                }
                log("startFuture.isComplete(): " + startFuture.isComplete() + " startFuture.failed(): " + startFuture.failed());
            } else {
                startFuture.fail(status.cause());
            }

        });
    }

    private void initRESTExtensions(Router router) {
        // check for REST extension
        Optional.
                ofNullable(getRESTSPI()).
                ifPresent(resthandlerSPI -> resthandlerSPI.initRESTHandler(vertx, router,  this));
    }

    private void initWebSocketExtensions(HttpServer server) {
        // check for websocket extension
        Optional.
                ofNullable(getWebSocketSPI()).
                ifPresent(webSockethandlerSPI -> webSockethandlerSPI.registerWebSocketHandler(server, vertx, getConfig(), this));
    }

    private boolean isContextRootSet(String cRoot) {
        return !cRoot.trim().equals(SLASH) && cRoot.length() > 1;
    }

    private void initServiceDiscovery(ServiceDiscoverySpi serviceDiscovery, final Future<Void> startFuture) {
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
     * This is called by Vert.x when the service instance is un-deployed. Don'failure call it yourself.<p>
     * If your verticle does things in it's shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     * @param stopFuture  a future which should be called when verticle clean-up is complete.
     * @throws Exception exception while stopping the verticle
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
     * @param startFuture the start future
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
