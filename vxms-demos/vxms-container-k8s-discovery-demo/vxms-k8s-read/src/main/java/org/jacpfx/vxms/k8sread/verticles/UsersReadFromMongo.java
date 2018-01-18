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

package org.jacpfx.vxms.k8sread.verticles;

import org.jacpfx.vxms.k8sread.util.DefaultResponses;
import org.jacpfx.vxms.k8sread.util.InitMongoDB;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/**
 * Created by Andy Moncsek on 17.02.16.
 */
@ServiceEndpoint(name = "read-verticle", contextRoot = "/read", port = 7070)
public class UsersReadFromMongo extends VxmsEndpoint {

  Logger log = Logger.getLogger(UsersReadFromMongo.class.getName());
  private MongoClient mongo;



  @Override
  public void postConstruct(final Future<Void> startFuture) {
    mongo = InitMongoDB.initMongoData(vertx, config());
    startFuture.complete();
  }

  @Path("/api/users")
  @GET
  public void getAllUsers(RestHandler reply) {
    reply.
        response().
        stringResponse(this::findAllUsers).
        timeout(2000).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((failure, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(failure.getMessage()).
                encodePrettily())
        ).
        execute();
  }

  public MongoClient findAllUsers(Future<String> future) {
    return mongo.find("users", new JsonObject(), lookup -> {
      // error handling
      if (lookup.failed()) {
        future.fail(lookup.cause());
      } else {
        future.complete(new JsonArray(lookup.
            result().
            stream().
            collect(Collectors.toList())).
            encode());
      }

    });
  }

  @Path("/api/users/:id")
  @GET
  public void getUserById(RestHandler reply) {
    final String id = reply.request().param("id");
    if (id == null || id.isEmpty()) {
      reply.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    reply.
        response().
        stringResponse(future -> findUser(id, future)).
        timeout(2000).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((failure, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(failure.getMessage()).
                encodePrettily())
        ).
        execute();
  }

  public MongoClient findUser(String id, Future<String> future) {
    return mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
      // error handling
      if (lookup.failed()) {
        future.fail(lookup.cause());
      } else if (lookup.result() != null) {
        future.complete(lookup.result().encode());
      } else {
        future.fail("no user found");
      }

    });
  }


  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

    DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(
        new JsonObject().put("local", true));

    Vertx.vertx().deployVerticle(UsersReadFromMongo.class.getName(),options);
  }
}
