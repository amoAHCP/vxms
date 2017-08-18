/*
 * Copyright [2017] [Andy Moncsek]
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

package org.jacpfx.vertx.services;

import static org.jacpfx.vertx.util.ServiceUtil.getEndpointConfiguration;
import static org.jacpfx.vertx.util.ServiceUtil.getEventBusSPI;
import static org.jacpfx.vertx.util.ServiceUtil.getRESTSPI;
import static org.jacpfx.vertx.util.ServiceUtil.getWebSocketSPI;

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
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.concurrent.LocalData;
import org.jacpfx.common.configuration.EndpointConfiguration;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.common.util.ReflectionExecutionWrapper;
import org.jacpfx.common.util.URIUtil;

/**
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project. This class
 * can be extended to create a vxms service Created by Andy Moncsek
 */
public abstract class VxmsEndpoint extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(VxmsEndpoint.class);

  // private VxmsShared vxmsShared;

  @Override
  public final void start(final Future<Void> startFuture) {
    // register info (keepAlive) handler
    vertx.eventBus()
        .consumer(ConfigurationUtil.getServiceName(getConfig(), this.getClass()) + "-info",
            this::info);
    initEndpoint(startFuture, this, new VxmsShared(vertx, new LocalData(vertx)));

  }

  /**
   * Initialize an existing Vert.x instance as a Vxms endpoint so you can use an Verticle (Extending
   * AbstractVerticle) as a fully functional Vxms Endpoint). Caution: this start methods completes
   * the start future when it's ready, so it must be the last initialisation (order & callback
   * wise)
   */
  public void start(final Future<Void> startFuture, AbstractVerticle registrationObject) {
    // initEndpoint(startFuture,registrationObject);
  }


  /**
   * initiate Endpoint  and all Plugins
   *
   * @param startFuture, the Vertx start feature
   */
  private void initEndpoint(final Future<Void> startFuture, AbstractVerticle registrationObject,
      VxmsShared vxmsShared) {
    final Class<? extends AbstractVerticle> serviceClass = registrationObject.getClass();
    final int port = ConfigurationUtil.getEndpointPort(getConfig(), serviceClass);
    final String host = ConfigurationUtil.getEndpointHost(getConfig(), serviceClass);
    final String contexRoot = ConfigurationUtil.getContextRoot(getConfig(), serviceClass);
    final CustomServerOptions endpointConfig = ConfigurationUtil.getEndpointOptions(serviceClass);
    final HttpServerOptions options = endpointConfig.getServerOptions(registrationObject.config());
    final Vertx vertx = vxmsShared.getVertx();
    final HttpServer server = vertx.createHttpServer(options.setHost(host).setPort(port));

    final boolean secure = options.isSsl();
    final boolean contextRootSet = URIUtil
        .isContextRootSet(Optional.ofNullable(contexRoot).orElse(""));
    final Router topRouter = Router.router(vertx);
    final Router subRouter = contextRootSet ? Router.router(vertx) : null;
    final Router router = contextRootSet ? subRouter : topRouter;
    final EndpointConfiguration endpointConfiguration = getEndpointConfiguration(
        registrationObject);

    getConfig().put("secure", secure);

    initEndoitConfiguration(endpointConfiguration, vertx, router, secure, host, port);

    initHandlerSPIs(server, router, registrationObject, vxmsShared);

    postEndoitConfiguration(endpointConfiguration, router);

    if (contextRootSet) {
      topRouter
          .mountSubRouter(URIUtil.getCleanContextRoot(Optional.ofNullable(contexRoot).orElse("")),
              subRouter);
    }

    if (port != 0) {
      log("create http server: " + options.getHost() + ":" + options.getPort());
      initHTTPEndpoint(registrationObject, startFuture, port, host, server, topRouter);
    } else {
      initNoHTTPEndpoint(registrationObject, startFuture, topRouter);
    }

  }

  private void initNoHTTPEndpoint(AbstractVerticle registrationObject, Future<Void> startFuture,
      Router topRouter) {
    startFuture.setHandler(result -> logStartfuture(startFuture));
    executePostConstruct(registrationObject, topRouter, startFuture);
  }

  private void initHandlerSPIs(HttpServer server, Router router,
      AbstractVerticle registrationObject, VxmsShared vxmsShared) {
    initWebSocketExtensions(server, registrationObject);
    initRESTExtensions(router, registrationObject, vxmsShared);
    initEventBusExtensions(registrationObject, vxmsShared);
  }

  /**
   * starts the HTTP Endpoint
   *
   * @param startFuture the vertx start future
   * @param port the port to listen
   * @param host the host to bind
   * @param server the vertx server
   * @param topRouter the router object
   */
  private void initHTTPEndpoint(AbstractVerticle registrationObject, Future<Void> startFuture,
      int port, String host, HttpServer server,
      Router topRouter) {
    startFuture.setHandler(result -> logStartfuture(startFuture));
    server.requestHandler(topRouter::accept).listen(status -> {
      if (status.succeeded()) {
        log("started on PORT: " + port + " host: " + host);
        executePostConstruct(registrationObject, topRouter, startFuture);
      } else {
        startFuture.fail(status.cause());
      }
    });
  }

  private void logStartfuture(Future<Void> startFuture) {
    final Throwable cause = startFuture.cause();
    String causeMessage = cause != null ? cause.getMessage() : "";
    log("startFuture.isComplete(): " + startFuture.isComplete() + " startFuture.failed(): "
        + startFuture.failed() + " message:" + causeMessage);
  }

  private void initRESTExtensions(Router router, AbstractVerticle registrationObject,
      VxmsShared vxmsShared) {
    // check for REST extension
    Optional.
        ofNullable(getRESTSPI()).
        ifPresent(resthandlerSPI -> resthandlerSPI
            .initRESTHandler(vxmsShared, router, registrationObject));
  }

  private void initEventBusExtensions(AbstractVerticle registrationObject, VxmsShared vxmsShared) {
    // check for REST extension
    Optional.
        ofNullable(getEventBusSPI()).
        ifPresent(eventbusHandlerSPI -> eventbusHandlerSPI
            .initEventHandler(vxmsShared, registrationObject));
  }

  private void initWebSocketExtensions(HttpServer server, AbstractVerticle registrationObject) {
    // check for websocket extension
    Optional.
        ofNullable(getWebSocketSPI()).
        ifPresent(webSockethandlerSPI -> webSockethandlerSPI
            .registerWebSocketHandler(server, vertx, getConfig(), registrationObject));
  }


  /**
   * Stop the service.<p> This is called by Vert.x when the service instance is un-deployed.
   * Don'failure call it yourself.<p> If your verticle does things in it's shut-down which take some
   * time then you can override this method and call the stopFuture some time later when clean-up is
   * complete.
   *
   * @param stopFuture a future which should be called when verticle clean-up is complete.
   * @throws Exception exception while stopping the verticle
   */
  public final void stop(Future<Void> stopFuture) throws Exception {
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
    methodWrapperToInvoke.ifPresent(wrapper -> wrapper.invoke());

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


  private void initEndoitConfiguration(EndpointConfiguration endpointConfiguration, Vertx vertx,
      Router router, boolean secure, String host, int port) {
    Optional.of(endpointConfiguration).ifPresent(endpointConfig -> {

      endpointConfig.corsHandler(router);

      endpointConfig.bodyHandler(router);

      endpointConfig.cookieHandler(router);

      endpointConfig.sessionHandler(vertx, router);

      endpointConfig.customRouteConfiguration(vertx, router, secure, host, port);
    });
  }

  private void postEndoitConfiguration(EndpointConfiguration endpointConfiguration, Router router) {
    Optional.of(endpointConfiguration)
        .ifPresent(endpointConfig -> endpointConfig.staticHandler(router));
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
