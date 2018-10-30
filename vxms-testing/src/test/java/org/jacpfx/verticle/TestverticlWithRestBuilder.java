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

package org.jacpfx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/** Created by amo on 23.11.16. */
public class TestverticlWithRestBuilder extends AbstractVerticle {

  public static void main(String[] args) {
    DeploymentOptions options =
        new DeploymentOptions()
            .setInstances(1)
            .setConfig(
                new JsonObject()
                    .put("host", "localhost")
                    .put("serverOptions", "org.jacpfx.verticle.MyCustomServerOptions"));
    Vertx.vertx().deployVerticle(TestverticlWithRestBuilder.class.getName(), options);
  }

  @Override
  public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
    VxmsEndpoint.start(startFuture, this);
  }

  @Path("/hello")
  @GET
  public void hello(RestHandler handler) {
    handler.response().stringResponse((future) -> future.complete("hi")).execute();
  }

  @Path("/hello2")
  @GET
  public void hello2(RestHandler handler) {
    handler.response().stringResponse((future) -> future.complete("hi")).execute();
  }
}
