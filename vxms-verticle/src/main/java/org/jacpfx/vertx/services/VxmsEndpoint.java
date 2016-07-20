package org.jacpfx.vertx.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import or.jacpfx.spi.ServiceDiscoverySpi;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.ServiceInfo;
import org.jacpfx.common.Type;
import org.jacpfx.common.configuration.DefaultEndpointConfiguration;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.common.configuration.EndpointConfiguration;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.vertx.rest.util.RESTInitializer;
import org.jacpfx.vertx.services.util.MetadataUtil;
import org.jacpfx.vertx.websocket.registry.LocalWebSocketRegistry;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.WebSocketInitializer;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project
 * Created by amo on 28.10.14.
 */
public abstract class VxmsEndpoint extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(VxmsEndpoint.class);
    private String host;
    private boolean secure;
    private CustomServerOptions endpointConfig;
    private ServiceInfo descriptor;
    private boolean clustered;
    private int port = 0;
    private WebSocketRegistry webSocketRegistry;
    private Consumer<Future<Void>> onStop;
    private ServiceDiscoverySpi serviceDiscovery;

    @Override
    public final void start(final Future<Void> startFuture) {
        long startTime = System.currentTimeMillis();

        // register info (keepAlive) handler
        vertx.eventBus().consumer(ConfigurationUtil.serviceName(getConfig(), this.getClass()) + "-info", this::info);

        initSelfHostedService(startFuture);

        long endTime = System.currentTimeMillis();
        log.info("start time: " + (endTime - startTime) + "ms");


    }


    private void initSelfHostedService(final Future<Void> startFuture) {
        port = ConfigurationUtil.getEndpointPort(getConfig(), this.getClass());
        host = ConfigurationUtil.getEndpointHost(getConfig(), this.getClass());
        endpointConfig = ConfigurationUtil.getEndpointOptions(this.getClass());
        final HttpServerOptions options = endpointConfig.getOptions(this.getConfig());
        secure = options.isSsl();
        getConfig().put("secure",secure);
        // collect all service operations in service for descriptor

        descriptor = MetadataUtil.createInfoObject(port, getConfig(), this.getClass());
        updateConfigurationToSelfhosted();

        clustered = getConfig().getBoolean("clustered", false);

        HttpServer server = vertx.
                createHttpServer(options.setHost(host).setPort(port));


        Router router = Router.router(vertx);
        final EndpointConfiguration endpointConfiguration = getEndpointConfiguration(this);

        initEndoitConfiguration(endpointConfiguration,vertx, router);

        // TODO move to SPI
        initWebSocket(server);
        // TODO move to SPI
        initRest(router);

        postEndoitConfiguration(endpointConfiguration, router);

        server.requestHandler(router::accept).listen(status -> {
            if (status.succeeded()) {
                log("started on PORT: " + port + " host: " + host);
                serviceDiscovery = getServiceDiscoverySPI();
                if (serviceDiscovery!=null) {
                    handleServiceRegistration(startFuture);
                } else {
                    postConstruct(router, startFuture);
                }
                log("startFuture.isComplete(): " + startFuture.isComplete() + " startFuture.failed(): " + startFuture.failed());
            } else {
                startFuture.fail(status.cause());
            }

        });
    }

    private void handleServiceRegistration(final Future<Void> startFuture) {
        final AbstractVerticle current = this;
        Optional.ofNullable(serviceDiscovery).ifPresent(sDicovery -> {
            sDicovery.registerService(() -> postConstruct(startFuture), ex -> {
                if (!startFuture.isComplete()) startFuture.fail(ex);
            }, current);
            this.onStop = (stopFuture) -> sDicovery.disconnect();
        });

    }


    public final void stop(Future<Void> stopFuture) throws Exception {
        Optional.ofNullable(onStop).ifPresent(stop -> stop.accept(stopFuture));
        if (!stopFuture.isComplete()) stopFuture.complete();
    }

    private ServiceDiscoverySpi getServiceDiscoverySPI() {
        ServiceLoader<ServiceDiscoverySpi> loader = ServiceLoader.load(ServiceDiscoverySpi.class);
        if (!loader.iterator().hasNext()) return null;
        return loader.iterator().next();
    }


    /**
     * Overwrite this method to handle your own initialisation after all vxms init is done
     *
     * @param router      the http router handler
     * @param startFuture
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

    private void initRest(Router router) {
        RESTInitializer.initRESTHandler(vertx, router, getConfig(), this);
    }

    private  void initEndoitConfiguration(EndpointConfiguration endpointConfiguration, Vertx vertx, Router router) {
        Optional.of(endpointConfiguration).ifPresent(endpointConfig -> {

            endpointConfig.corsHandler(router);

            endpointConfig.bodyHandler(router);

            endpointConfig.cookieHandler(router);

            endpointConfig.sessionHandler(vertx, router);

            endpointConfig.customRouteConfiguration(vertx, router);
        });
    }

    private  void postEndoitConfiguration(EndpointConfiguration endpointConfiguration, Router router) {
        Optional.of(endpointConfiguration).ifPresent(endpointConfig -> endpointConfig.staticHandler(router));
    }

    private  EndpointConfiguration getEndpointConfiguration(Object service) {
        EndpointConfiguration endpointConfig = null;
        if (service.getClass().isAnnotationPresent(EndpointConfig.class)) {
            final EndpointConfig annotation = service.getClass().getAnnotation(EndpointConfig.class);
            final Class<? extends EndpointConfiguration> epConfigClazz = annotation.value();
            try {
                endpointConfig = epConfigClazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return endpointConfig == null ? new DefaultEndpointConfiguration() : endpointConfig;
    }


    private void initWebSocket(HttpServer server) {
        final Object service = this;
        descriptor.getOperationsByType(Type.WEBSOCKET).findFirst().ifPresent(operation -> {
            webSocketRegistry = initWebSocketRegistryInstance();
            WebSocketInitializer.registerWebSocketHandler(server, vertx, webSocketRegistry, getConfig(), service);
        });
    }


    private WebSocketRegistry initWebSocketRegistryInstance() {
        if (clustered) {
            return null;
        } else {
            return new LocalWebSocketRegistry(this.vertx);
        }
    }

    private void updateConfigurationToSelfhosted() {
        getConfig().put("selfhosted", true);
        getConfig().put("selfhosted-host", ConfigurationUtil.serviceName(getConfig(), this.getClass()));
    }


    private void log(final String value) {
        log.info(value);
    }


    private void info(Message m) {

        try {
            m.reply(Serializer.serialize(getServiceDescriptor()), new DeliveryOptions().setSendTimeout(10000));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private ServiceInfo getServiceDescriptor() {
        return this.descriptor;
    }


    private JsonObject getConfig() {
        return context != null ? context.config() : new JsonObject();
    }


    // TODO add versioning to service
    protected String getVersion() {
        return null;
    }

}
