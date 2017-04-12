package org.jacpfx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by amo on 23.11.16.
 */
public class Testverticle extends AbstractVerticle {

  public static void main(String[] args) {
    DeploymentOptions options = new DeploymentOptions().setInstances(2)
        .setConfig(new JsonObject().put("host", "localhost"));
    Vertx.vertx().deployVerticle(Testverticle.class.getName(), options);
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    final HttpServer localhost = vertx
        .createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080));
    final Router router = Router.router(vertx);
    router.get("/test").handler(handler -> {
      handler.response().end("hello");
    });
    localhost.requestHandler(router::accept).listen();
    startFuture.complete();
  }
}
