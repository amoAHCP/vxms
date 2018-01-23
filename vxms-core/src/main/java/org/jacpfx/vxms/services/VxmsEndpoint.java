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

import static org.jacpfx.vxms.common.util.ConfigurationUtil.getRouterConfiguration;
import static org.jacpfx.vxms.common.util.ServiceUtil.getDiscoverySPI;
import static org.jacpfx.vxms.common.util.ServiceUtil.getEventBusSPI;
import static org.jacpfx.vxms.common.util.ServiceUtil.getRESTSPI;
import static org.jacpfx.vxms.common.util.ServiceUtil.getWebSocketSPI;

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
import java.util.Optional;
import java.util.stream.Stream;
import org.jacpfx.vxms.common.CustomServerOptions;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.concurrent.LocalData;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;
import org.jacpfx.vxms.common.util.ConfigurationUtil;
import org.jacpfx.vxms.common.util.ReflectionExecutionWrapper;
import org.jacpfx.vxms.common.util.URIUtil;

/**
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project. This class
 * can be extended to create a vxms service Created by Andy Moncsek
 */
public abstract class VxmsEndpoint extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(VxmsEndpoint.class);

  @Override
  public final void start(final Future<Void> startFuture) {
    // register info (keepAlive) handler
    vertx.eventBus()
        .consumer(ConfigurationUtil.getServiceName(getConfig(), this.getClass()) + "-info",
            VxmsEndpoint::info);
    initEndpoint(startFuture, this, new VxmsShared(vertx, new LocalData(vertx)));

  }

  /**
   * Initialize an existing Vert.x instance as a Vxms endpoint so you can use an Verticle (Extending
   * AbstractVerticle) as a fully functional Vxms Endpoint). Caution: this start methods completes
   * the start future when it's ready, so it must be the last initialisation (order and callback
   * wise)
   * @param startFuture the Start Future from Vert.x
   * @param registrationObject the AbstractVerticle to register (mostly this)
   */
  public static void start(final Future<Void> startFuture, AbstractVerticle registrationObject) {
    final Vertx vertx = registrationObject.getVertx();
    final JsonObject config = vertx.getOrCreateContext().config();
    vertx.eventBus()
        .consumer(ConfigurationUtil.getServiceName(config, registrationObject.getClass()) + "-info",
            VxmsEndpoint::info);
    initEndpoint(startFuture, registrationObject, new VxmsShared(vertx, new LocalData(vertx)));
  }


  /**
   * initiate Endpoint and all Extensions
   *
   * @param startFuture, the Vertx start feature
   * @param registrationObject, the verticle to initialize
   * @param vxmsShared, the {@link VxmsShared} object
   */
  private static void initEndpoint(final Future<Void> startFuture, AbstractVerticle registrationObject,
      VxmsShared vxmsShared) {
    final Vertx vertx = vxmsShared.getVertx();
    final JsonObject config = vertx.getOrCreateContext().config();
    final Class<? extends AbstractVerticle> serviceClass = registrationObject.getClass();
    final int port = ConfigurationUtil.getEndpointPort(config, serviceClass);
    final String host = ConfigurationUtil.getEndpointHost(config, serviceClass);
    final String contextRoot = ConfigurationUtil.getContextRoot(config, serviceClass);
    final CustomServerOptions endpointConfig = ConfigurationUtil.getEndpointOptions(config, serviceClass);
    final HttpServerOptions options = endpointConfig.getServerOptions(registrationObject.config());

    final HttpServer server = vertx.createHttpServer(options.setHost(host).setPort(port));

    final boolean secure = options.isSsl();
    final boolean contextRootSet = URIUtil
        .isContextRootSet(Optional.ofNullable(contextRoot).orElse(""));
    final Router topRouter = Router.router(vertx);
    final Router subRouter = contextRootSet ? Router.router(vertx) : null;
    final Router router = contextRootSet ? subRouter : topRouter;
    final RouterConfiguration routerConfiguration = getRouterConfiguration(
        config,serviceClass);

    config.put("secure", secure);

    initEndpointConfiguration(routerConfiguration, vertx, router, secure, host, port);

    initExtensions(server, router, registrationObject, vxmsShared);

    postEndpointConfiguration(routerConfiguration, router);

    if (contextRootSet) {
      topRouter
          .mountSubRouter(URIUtil.getCleanContextRoot(Optional.ofNullable(contextRoot).orElse("")),
              subRouter);
    }

    if (port != 0) {
      log("create http server: " + options.getHost() + ":" + options.getPort());
      initHTTPEndpoint(registrationObject, startFuture, server, topRouter);
    } else {
      initNoHTTPEndpoint(registrationObject, startFuture, topRouter);
    }

  }

  private static void initNoHTTPEndpoint(AbstractVerticle registrationObject, Future<Void> startFuture,
      Router topRouter) {
    startFuture.setHandler(result -> logStartfuture(startFuture));
    executePostConstruct(registrationObject, topRouter, startFuture);
  }

  private static void initExtensions(HttpServer server, Router router,
      AbstractVerticle registrationObject, VxmsShared vxmsShared) {
    initDiscoveryxtensions(registrationObject);
    initWebSocketExtensions(server, registrationObject,vxmsShared);
    initRESTExtensions(router, registrationObject, vxmsShared);
    initEventBusExtensions(registrationObject, vxmsShared);
  }

  /**
   * starts the HTTP Endpoint
   *
   * @param startFuture the vertx start future
   * @param server the vertx server
   * @param topRouter the router object
   */
  private static void initHTTPEndpoint(AbstractVerticle registrationObject,
      Future<Void> startFuture,HttpServer server,
      Router topRouter) {
    server.requestHandler(topRouter::accept).listen(status -> {
      if (status.succeeded()) {
        executePostConstruct(registrationObject, topRouter, startFuture);
        startFuture.setHandler(result -> logStartfuture(startFuture));
      } else {
        startFuture.fail(status.cause());
        startFuture.setHandler(result -> logStartfuture(startFuture));
      }
    });

  }

  private static void logStartfuture(Future<Void> startFuture) {
    final Throwable cause = startFuture.cause();
    String causeMessage = cause != null ? cause.getMessage() : "";
    if(!startFuture.failed()) {
      log("startFuture.isComplete(): " + startFuture.isComplete());
    } else {
      log("startFuture.failed(): "
          + startFuture.failed() + " message: " + causeMessage);
    }

  }

  private static void initRESTExtensions(Router router, AbstractVerticle registrationObject,
      VxmsShared vxmsShared) {
    // check for REST extension
    Optional.
        ofNullable(getRESTSPI()).
        ifPresent(resthandlerSPI -> {
          resthandlerSPI
              .initRESTHandler(vxmsShared, router, registrationObject);
          log("start REST extension");
        });
  }

  private static void initEventBusExtensions(AbstractVerticle registrationObject,
      VxmsShared vxmsShared) {
    // check for Eventbus extension
    Optional.
        ofNullable(getEventBusSPI()).
        ifPresent(eventbusHandlerSPI -> {
          eventbusHandlerSPI
              .initEventHandler(vxmsShared, registrationObject);
          log("start event-bus extension");
        });
  }

  private static void initWebSocketExtensions(HttpServer server,
      AbstractVerticle registrationObject, VxmsShared vxmsShared) {
    // check for websocket extension
    final Vertx vertx = vxmsShared.getVertx();
    final JsonObject config = vertx.getOrCreateContext().config();
    Optional.
        ofNullable(getWebSocketSPI()).
        ifPresent(webSockethandlerSPI -> {
          webSockethandlerSPI
              .registerWebSocketHandler(server, vertx, config, registrationObject);
          log("start websocket extension");
        });
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
  public final void stop(Future<Void> stopFuture) {
    if (!stopFuture.isComplete()) {
      stopFuture.complete();
    }
  }


  /**
   * Executes the postConstruct using Reflection. This solves the issue that you can extend from a
   * VxmsEndpoint or use the static invocation of an AbstractVerticle.
   *
   * @param router the http router handler
   * @param startFuture the vert.x start future
   * @param registrationObject the object to execute postConstruct
   */
  private static void executePostConstruct(AbstractVerticle registrationObject, Router router,
      final Future<Void> startFuture) {
    final Stream<ReflectionExecutionWrapper> reflectionExecutionWrapperStream = Stream
        .of(new ReflectionExecutionWrapper("postConstruct",
                registrationObject, new Object[]{router, startFuture}, startFuture),
            new ReflectionExecutionWrapper("postConstruct",
                registrationObject, new Object[]{startFuture, router}, startFuture),
            new ReflectionExecutionWrapper("postConstruct",
                registrationObject, new Object[]{startFuture}, startFuture));
    final Optional<ReflectionExecutionWrapper> methodWrapperToInvoke = reflectionExecutionWrapperStream
        .filter(ReflectionExecutionWrapper::isPresent).findFirst();
    methodWrapperToInvoke.ifPresent(ReflectionExecutionWrapper::invoke);
    if(!methodWrapperToInvoke.isPresent() && !startFuture.isComplete()) {
      startFuture.complete();
    }

  }

  /**
   * Overwrite this method to handle your own initialisation after all vxms init is done
   *
   * @param router the http router handler
   * @param startFuture the vert.x start future
   */
  public void postConstruct(Router router, final Future<Void> startFuture) {
    postConstruct(startFuture);
  }

  /**
   * Overwrite this method to handle your own initialisation after all vxms init is done
   *
   * @param startFuture the start future
   */
  public void postConstruct(final Future<Void> startFuture) {
    startFuture.complete();
  }


  private static void initEndpointConfiguration(RouterConfiguration routerConfiguration,
      Vertx vertx,
      Router router, boolean secure, String host, int port) {
    Optional.of(routerConfiguration).ifPresent(endpointConfig -> {

      endpointConfig.corsHandler(router);

      endpointConfig.bodyHandler(router);

      endpointConfig.cookieHandler(router);

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
