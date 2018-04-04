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

package org.jacpfx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/** Created by amo on 23.11.16. */
public class Testverticle extends AbstractVerticle {

  public static void main(String[] args) {
    DeploymentOptions options =
        new DeploymentOptions()
            .setInstances(2)
            .setConfig(new JsonObject().put("host", "localhost"));
    Vertx.vertx().deployVerticle(Testverticle.class.getName(), options);
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    final HttpServer localhost =
        vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080));
    final Router router = Router.router(vertx);
    router
        .get("/test")
        .handler(
            handler -> {
              handler.response().end("hello");
            });
    localhost.requestHandler(router::accept).listen();
    startFuture.complete();
  }
}
