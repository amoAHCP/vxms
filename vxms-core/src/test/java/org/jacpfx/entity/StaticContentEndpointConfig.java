package org.jacpfx.entity;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.jacpfx.common.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class StaticContentEndpointConfig implements EndpointConfiguration {


  public void staticHandler(Router router) {
    router.route("/static/*").handler(StaticHandler.create());
    // Create a router endpoint for the static content.
    // router.route().handler(StaticHandler.create());
  }

  public BodyHandler bodyHandler() {
    return BodyHandler.create();
  }
}