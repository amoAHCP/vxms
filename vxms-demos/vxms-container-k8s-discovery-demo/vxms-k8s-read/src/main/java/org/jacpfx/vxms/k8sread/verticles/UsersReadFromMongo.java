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

package org.jacpfx.vxms.k8sread.verticles;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vertx.spring.SpringVerticle;
import org.jacpfx.vertx.spring.SpringVerticleFactory;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.k8sread.ReadApplication;
import org.jacpfx.vxms.k8sread.service.UserService;
import org.jacpfx.vxms.k8sread.util.DefaultResponses;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.springframework.beans.factory.annotation.Autowired;


@SpringVerticle(springConfig = ReadApplication.class)
@ServiceEndpoint(name = "read-verticle", contextRoot = "/read", port = 7070)
public class UsersReadFromMongo extends VxmsEndpoint {

  Logger log = Logger.getLogger(UsersReadFromMongo.class.getName());

  @Autowired private UserService service;

  @Override
  public void postConstruct(final Future<Void> startFuture) {
    SpringVerticleFactory.initSpring(this);

    service.initData(startFuture);
  }

  @Path("/api/users")
  @GET
  public void getAllUsers(RestHandler reply) {
    reply
        .response()
        .stringResponse(future -> service.findAllUsers(future))
        .timeout(2000)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (failure, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(failure.getMessage()).encodePrettily()))
        .execute();
  }

  @Path("/api/users/:id")
  @GET
  public void getUserById(RestHandler reply) {
    final String id = reply.request().param("id");
    if (id == null || id.isEmpty()) {
      reply.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    reply
        .response()
        .stringResponse(future -> service.findUser(id, future))
        .timeout(2000)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (failure, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(failure.getMessage()).encodePrettily()))
        .execute();
  }

  @Path("/health")
  @GET
  public void health(RestHandler handler) {
    handler
        .response()
        .stringResponse(this::checkHealth)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond((onError, future) -> future.complete(""))
        .httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE)
        .execute(HttpResponseStatus.OK);
  }

  private void checkHealth(Future<String> future) {
    future.complete("Ready");
  }

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

    DeploymentOptions options =
        new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true));

    Vertx.vertx().deployVerticle(UsersReadFromMongo.class.getName(), options);
  }
}
