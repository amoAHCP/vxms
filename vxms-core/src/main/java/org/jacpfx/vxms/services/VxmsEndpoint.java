/*
 * Copyright [2018] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

import io.vertx.core.json.JsonObject;

import io.vertx.ext.web.Router;
import org.jacpfx.vxms.common.CustomServerOptions;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.concurrent.LocalData;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;
import org.jacpfx.vxms.common.util.ConfigurationUtil;
import org.jacpfx.vxms.common.util.ReflectionExecutionWrapper;
import org.jacpfx.vxms.common.util.StreamUtils;
import org.jacpfx.vxms.common.util.URIUtil;
import org.jacpfx.vxms.spi.VxmsRoutes;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.jacpfx.vxms.common.util.ConfigurationUtil.getRouterConfiguration;
import static org.jacpfx.vxms.common.util.ServiceUtil.*;

/**
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project. This class
 * can be extended to create a vxms service Created by Andy Moncsek
 */
public abstract class VxmsEndpoint extends AbstractVerticle {

    private static final Logger log = Logger.getLogger(VxmsEndpoint.class.getName());

    @Override
    public final void start(final Promise<Void> startPromise) {
        // register info (keepAlive) handler
        vertx.eventBus()
                .consumer(ConfigurationUtil.getServiceName(getConfig(), this.getClass()) + "-info",
                        VxmsEndpoint::info);
        initEndpoint(startPromise, this, new VxmsShared(vertx, new LocalData(vertx)));

    }

    /**
     * Initialize an existing Vert.x instance as a Vxms endpoint so you can use an Verticle (Extending
     * AbstractVerticle) as a fully functional Vxms Endpoint). Caution: this start methods completes
     * the start future when it's ready, so it must be the last initialisation (order and callback
     * wise)
     *
     * @param startPromise        the Start Future from Vert.x
     * @param registrationObject the AbstractVerticle to register (mostly this)
     */
    public static void start(final Promise<Void> startPromise, AbstractVerticle registrationObject) {
        final Vertx vertx = registrationObject.getVertx();
        final JsonObject config = vertx.getOrCreateContext().config();
        vertx.eventBus()
                .consumer(ConfigurationUtil.getServiceName(config, registrationObject.getClass()) + "-info",
                        VxmsEndpoint::info);
        initEndpoint(startPromise, registrationObject, new VxmsShared(vertx, new LocalData(vertx)));
    }


    public static void init(final Promise<Void> startPromise, AbstractVerticle registrationObject,
                            VxmsRoutes... routes) {
        // TODO to be used for build in REST and others
        final Vertx vertx = registrationObject.getVertx();
        final JsonObject config = vertx.getOrCreateContext().config();
        vertx.eventBus()
                .consumer(ConfigurationUtil.getServiceName(config, registrationObject.getClass()) + "-info",
                        VxmsEndpoint::info);
        initEndpoint(startPromise, registrationObject, new VxmsShared(vertx, new LocalData(vertx)),
                routes);
    }


    /**
     * initiate Endpoint and all Extensions
     *
     * @param startPromise,        the Vertx start feature
     * @param registrationObject, the verticle to initialize
     * @param vxmsShared,         the {@link VxmsShared} object
     */
    private static void initEndpoint(final Promise<Void> startPromise,
                                     final AbstractVerticle registrationObject,
                                     final VxmsShared vxmsShared, final VxmsRoutes... routes) {
        final Vertx vertx = vxmsShared.getVertx();
        final JsonObject config = vertx.getOrCreateContext().config();
        final Class<? extends AbstractVerticle> serviceClass = registrationObject.getClass();
        final int port = ConfigurationUtil.getEndpointPort(config, serviceClass);
        final String host = ConfigurationUtil.getEndpointHost(config, serviceClass);
        final String contextRoot = ConfigurationUtil.getContextRoot(config, serviceClass);
        final CustomServerOptions endpointConfig = ConfigurationUtil
                .getEndpointOptions(config, serviceClass);
        final HttpServerOptions options = endpointConfig.getServerOptions(registrationObject.config());

        final HttpServer server = vertx.createHttpServer(options.setHost(host).setPort(port));

        final boolean secure = options.isSsl();
        final boolean contextRootSet = URIUtil
                .isContextRootSet(Optional.ofNullable(contextRoot).orElse(""));
        final Router topRouter = Router.router(vertx);
        final Router subRouter = contextRootSet ? Router.router(vertx) : null;
        final Router router = contextRootSet ? subRouter : topRouter;
        final RouterConfiguration routerConfiguration = getRouterConfiguration(
                config, serviceClass);

        config.put("secure", secure);

        initEndpointConfiguration(routerConfiguration, vertx, router, secure, host, port);

        initExtensions(server, router, registrationObject, vxmsShared, routes);

        postEndpointConfiguration(routerConfiguration, router);

        if (contextRootSet) {
            topRouter
                    .mountSubRouter(URIUtil.getCleanContextRoot(Optional.ofNullable(contextRoot).orElse("")),
                            subRouter);
        }

        if (port != 0) {
            log("create http server: " + options.getHost() + ":" + options.getPort());
            initHTTPEndpoint(registrationObject, startPromise, server, topRouter);
        } else {
            initNoHTTPEndpoint(registrationObject, startPromise, topRouter);
        }

    }

    private static void initNoHTTPEndpoint(AbstractVerticle registrationObject,
                                           Promise<Void> startPromise,
                                           Router topRouter) {
        startPromise.future().onComplete(result -> logStartfuture(startPromise.future()));
        executePostConstruct(registrationObject, topRouter, startPromise);
    }

    private static void initExtensions(HttpServer server, Router router,
                                       AbstractVerticle registrationObject, VxmsShared vxmsShared, VxmsRoutes... routes) {
        initDiscoveryxtensions(registrationObject);
        initWebSocketExtensions(server, registrationObject, vxmsShared);
        initRESTExtensions(router, registrationObject, vxmsShared, routes);
        initEventBusExtensions(registrationObject, vxmsShared);
    }

    /**
     * starts the HTTP Endpoint
     *
     * @param startPromise the vertx start future
     * @param server      the vertx server
     * @param topRouter   the router object
     */
    private static void initHTTPEndpoint(AbstractVerticle registrationObject,
                                         Promise<Void> startPromise, HttpServer server,
                                         Router topRouter) {
        server.requestHandler(topRouter).listen(status -> {
            if (status.succeeded()) {
                executePostConstruct(registrationObject, topRouter, startPromise);
            } else {
                startPromise.fail(status.cause());
            }
            startPromise.future().onComplete(result -> logStartfuture(startPromise.future()));
        });

    }

    private static void logStartfuture(Future<Void> startPromise) {
        final Throwable cause = startPromise.cause();
        String causeMessage = cause != null ? cause.getMessage() : "";
        if (!startPromise.failed()) {
            log("startPromise.isComplete(): " + startPromise.isComplete());
        } else {
            log("startPromise.failed(): "
                    + startPromise.failed() + " message: " + causeMessage);
        }

    }

    private static void initRESTExtensions(Router router, AbstractVerticle registrationObject,
                                           VxmsShared vxmsShared, VxmsRoutes... routes) {
        // check for REST extension
        Optional.
                ofNullable(getRESTSPI()).
                ifPresent(resthandlerSPI -> StreamUtils.asStream(resthandlerSPI).forEach(spi -> {
                    spi.initRESTHandler(vxmsShared, router, registrationObject, routes);
                    log("start REST extension: " + spi.getClass().getCanonicalName());
                }));
    }

    private static void initEventBusExtensions(AbstractVerticle registrationObject,
                                               VxmsShared vxmsShared) {
        // check for Eventbus extension
        Optional.
                ofNullable(getEventBusSPI()).
                ifPresent(eventbusHandlerSPI -> StreamUtils.asStream(eventbusHandlerSPI).forEach(spi -> {
                    spi
                            .initEventHandler(vxmsShared, registrationObject);
                    log("start event-bus extension: " + spi.getClass().getCanonicalName());
                }));
    }

    private static void initWebSocketExtensions(HttpServer server,
                                                AbstractVerticle registrationObject, VxmsShared vxmsShared) {
        // check for websocket extension
        final Vertx vertx = vxmsShared.getVertx();
        final JsonObject config = vertx.getOrCreateContext().config();
        Optional.
                ofNullable(getWebSocketSPI()).
                ifPresent(webSockethandlerSPI -> StreamUtils.asStream(webSockethandlerSPI).forEach(spi -> {
                    spi
                            .registerWebSocketHandler(server, vertx, config, registrationObject);
                    log("start websocket extension: " + spi.getClass().getCanonicalName());
                }));
    }

    private static void initDiscoveryxtensions(AbstractVerticle registrationObject) {
        // check for service discovery extension
        Optional.
                ofNullable(getDiscoverySPI()).
                ifPresent(discoverySPI -> {
                    discoverySPI.initDiscovery(registrationObject);
                    log("start discovery extension");
                });
    }


    /**
     * Stop the service.<p> This is called by Vert.x when the service instance is un-deployed.
     * Don'failure call it yourself.<p> If your verticle does things in it's shut-down which take some
     * time then you can override this method and call the stopFuture some time later when clean-up is
     * complete.
     *
     * @param stopFuture a future which should be called when verticle clean-up is complete.
     */
    public final void stop(Promise<Void> stopFuture) {
        if (!stopFuture.future().isComplete()) {
            stopFuture.complete();
        }
    }


    /**
     * Executes the postConstruct using Reflection. This solves the issue that you can extend from a
     * VxmsEndpoint or use the static invocation of an AbstractVerticle.
     *
     * @param router             the http router handler
     * @param startPromise        the vert.x start future
     * @param registrationObject the object to execute postConstruct
     */
    private static void executePostConstruct(AbstractVerticle registrationObject, Router router,
                                             final Promise<Void> startPromise) {
        final Stream<ReflectionExecutionWrapper> reflectionExecutionWrapperStream = Stream
                .of(new ReflectionExecutionWrapper("postConstruct",
                                registrationObject, new Object[]{router, startPromise}, startPromise),
                        new ReflectionExecutionWrapper("postConstruct",
                                registrationObject, new Object[]{startPromise, router}, startPromise),
                        new ReflectionExecutionWrapper("postConstruct",
                                registrationObject, new Object[]{startPromise}, startPromise));
        final Optional<ReflectionExecutionWrapper> methodWrapperToInvoke = reflectionExecutionWrapperStream
                .filter(ReflectionExecutionWrapper::isPresent).findFirst();
        methodWrapperToInvoke.ifPresent(ReflectionExecutionWrapper::invoke);
        if (!methodWrapperToInvoke.isPresent() && !startPromise.future().isComplete()) {
            startPromise.complete();
        }

    }

    /**
     * Overwrite this method to handle your own initialisation after all vxms init is done
     *
     * @param router      the http router handler
     * @param startFuture the vert.x start future
     */
    public void postConstruct(Router router, final Promise<Void> startFuture) {
        postConstruct(startFuture);
    }

    /**
     * Overwrite this method to handle your own initialisation after all vxms init is done
     *
     * @param startPromise the start future
     */
    public void postConstruct(final Promise<Void> startPromise) {
        startPromise.complete();
    }


    private static void initEndpointConfiguration(RouterConfiguration routerConfiguration,
                                                  Vertx vertx,
                                                  Router router, boolean secure, String host, int port) {
        Optional.of(routerConfiguration).ifPresent(endpointConfig -> {

            endpointConfig.corsHandler(router);

            endpointConfig.bodyHandler(router);

            endpointConfig.sessionHandler(vertx, router);

            endpointConfig.customRouteConfiguration(vertx, router, secure, host, port);
        });
    }

    private static void postEndpointConfiguration(RouterConfiguration routerConfiguration,
                                                  Router router) {
        Optional.of(routerConfiguration)
                .ifPresent(endpointConfig -> endpointConfig.staticHandler(router));
    }


    private static void log(final String value) {
        log.info(value);
    }


    private static void info(Message m) {
        // TODO create info message about service

    }


    private JsonObject getConfig() {
        return context != null ? context.config() : new JsonObject();
    }


}
