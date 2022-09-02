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

package org.jacpfx.vxms.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;



import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.RouteBuilder;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(port = 9090)
public class SimpleREST  extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    VxmsRESTRoutes routes =
            VxmsRESTRoutes.init()
                    .route(RouteBuilder.get("/helloGET", this::simpleRESTHello))
                    .route(RouteBuilder.get("/helloGET/:name", this::simpleRESTHelloWithParameter));
    VxmsEndpoint.init(startFuture, this, routes);
  }


  public void simpleRESTHello(RestHandler handler) {
    handler.
        response().
        stringResponse((response) -> response.complete("simple response")).
        execute();
  }

  public void simpleRESTHelloWithParameter(RestHandler handler) {

    handler.
        response().
        stringResponse((response) -> {
          final String name = handler.request().param("name");
          new Thread(()->{
              try {
                  Thread.sleep(2500);
              } catch (InterruptedException e) {
                  throw new RuntimeException(e);
              }
              response.complete("hello World 123 " + name);
          }).start();
    System.out.println("-------");

        }).
        timeout(100).
        onFailureRespond((error, future) -> future.complete("error, sorry")).
        httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
        retry(5).
        closeCircuitBreaker(2000).
        execute();
  }


  public static void main(String[] args) {
    DeploymentOptions options = new DeploymentOptions().setInstances(1)
        .setConfig(new JsonObject().put("host", "localhost"));
    Vertx.vertx().deployVerticle(SimpleREST.class.getName(), options);
  }
}
