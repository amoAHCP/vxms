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

package org.jacpfx.vxms.k8swrite.verticles;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.k8swrite.service.UserService;
import org.jacpfx.vxms.k8swrite.util.DefaultResponses;
import org.jacpfx.vxms.k8swrite.util.InitMongoDB;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/** Created by Andy Moncsek on 17.02.16. */
@ServiceEndpoint(name = "write-verticle", contextRoot = "/write", port = 9090)
public class UsersWriteToMongo extends VxmsEndpoint {

  Logger log = Logger.getLogger(UsersWriteToMongo.class.getName());
  private UserService service;

  @Override
  public void postConstruct(final Future<Void> startFuture) {
    service = new UserService(InitMongoDB.initMongoData(vertx, config()));
    startFuture.complete();
  }

  @Path("/api/users")
  @POST
  public void inertUser(RestHandler handler) {
    final JsonObject body = handler.request().body().toJsonObject();
    if (body == null || body.isEmpty()) {
      handler
          .response()
          .stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse("no content").encode()))
          .execute();
      return;
    }
    handler
        .response()
        .stringResponse(future -> service.handleInsert(body, future))
        .retry(2)
        .timeout(1000)
        .onError(t -> log.log(Level.WARNING, "ERROR: " + t.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encode()))
        .execute();
  }

  @Path("/api/users")
  @PUT
  public void updateUser(RestHandler handler) {
    final JsonObject user = handler.request().body().toJsonObject();
    if (user == null || user.isEmpty()) {
      handler
          .response()
          .stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse().encode()))
          .execute();
      return;
    }
    handler
        .response()
        .stringResponse(future -> service.handleUpdate(user, future))
        .retry(2)
        .timeout(1000)
        .onError(t -> log.log(Level.WARNING, "ERROR: " + t.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encode()))
        .execute();
  }

  @Path("/api/users/:id")
  @DELETE
  public void deleteUser(RestHandler handler) {
    final String id = handler.request().param("id");
    if (id == null || id.isEmpty()) {
      handler
          .response()
          .stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse().encode()))
          .execute();
      return;
    }
    handler
        .response()
        .stringResponse(future -> service.handleDelete(id, future))
        .retry(2)
        .timeout(1000)
        .onError(t -> log.log(Level.WARNING, "ERROR: " + t.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encode()))
        .execute();
  }

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

    DeploymentOptions options =
        new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true));

    Vertx.vertx().deployVerticle(UsersWriteToMongo.class.getName(), options);
  }
}
