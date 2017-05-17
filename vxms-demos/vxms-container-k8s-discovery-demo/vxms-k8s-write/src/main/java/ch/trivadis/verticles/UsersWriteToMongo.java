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

package ch.trivadis.verticles;

import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;


/**
 * Created by Andy Moncsek on 17.02.16.
 */
@ServiceEndpoint(name = "write-verticle", contextRoot = "/write", port = 8080)
public class UsersWriteToMongo extends VxmsEndpoint {

  Logger log = Logger.getLogger(UsersWriteToMongo.class.getName());
  private MongoClient mongo;


  @Override
  public void postConstruct(final Future<Void> startFuture) {
    mongo = InitMongoDB.initMongoData(vertx, config());
    startFuture.complete();
  }

  @Path("/api/users")
  @POST
  public void inertUser(RestHandler handler) {
    final JsonObject body = handler.request().body().toJsonObject();
    if (body == null || body.isEmpty()) {
      handler.response()
          .stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse("no content").encode()))
          .execute();
      return;
    }
    handler.
        response().
        stringResponse(future -> handleInsert(body, future)).
        retry(2).
        timeout(1000).
        onError(t -> log.log(Level.WARNING, "ERROR: " + t.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encode())
        ).
        execute();
  }

  private void handleInsert(final JsonObject newUser, Future<String> future) {
    mongo.findOne("users", new JsonObject().put("username", newUser.getString("username")), null,
        lookup -> {
          // error handling
          if (lookup.failed()) {
            future.fail(lookup.cause());
            return;
          }

          JsonObject user = lookup.result();
          if (user != null) {
            // already exists
            future.fail("user already exists");
          } else {
            mongo.insert("users", newUser, insert -> {
              // error handling
              if (insert.failed()) {
                future.fail("lookup failed");
                return;
              }
              // add the generated id to the user object
              newUser.put("_id", insert.result());
              future.complete(newUser.encode());
            });
          }
        });
  }

  @Path("/api/users")
  @PUT
  public void updateUser(RestHandler handler) {
    final JsonObject user = handler.request().body().toJsonObject();
    if (user == null || user.isEmpty()) {
      handler.response()
          .stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse().encode())).execute();
      return;
    }
    handler.
        response().
        stringResponse(future -> handleUpdate(user, future)).
        retry(2).
        timeout(1000).
        onError(t -> log.log(Level.WARNING, "ERROR: " + t.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encode())
        ).
        execute();
  }

  private void handleUpdate(final JsonObject user, Future<String> future) {
    final String id = user.getString("id");
    mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
      // error handling
      if (lookup.failed()) {
        future.fail(lookup.cause());
        return;
      }

      JsonObject existingUser = lookup.result();
      if (existingUser == null) {
        // does not exist
        future.fail("user does not exists");
      } else {
        // update the user properties
        existingUser.put("username", user.getString("username"));
        existingUser.put("firstName", user.getString("firstName"));
        existingUser.put("lastName", user.getString("lastName"));
        existingUser.put("address", user.getString("address"));

        mongo.replace("users", new JsonObject().put("_id", id), existingUser, replace -> {
          // error handling
          if (replace.failed()) {
            future.fail(lookup.cause());
            return;
          }
          future.complete(user.encode());
        });
      }
    });
  }

  @Path("/api/users/:id")
  @DELETE
  public void deleteUser(RestHandler handler) {
    final String id = handler.request().param("id");
    if (id == null || id.isEmpty()) {
      handler.response()
          .stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse().encode())).execute();
      return;
    }
    handler.
        response().
        stringResponse(future -> handleDelete(id, future)).
        retry(2).
        timeout(1000).
        onError(t -> log.log(Level.WARNING, "ERROR: " + t.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encode())
        ).
        execute();
  }

  private void handleDelete(String id, Future<String> future) {
    mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
      // error handling
      if (lookup.failed()) {
        future.fail(lookup.cause());
        return;
      }
      JsonObject user = lookup.result();
      if (user == null) {
        // does not exist
        future.fail("user does not exists");
      } else {
        mongo.remove("users", new JsonObject().put("_id", id), remove -> {
          // error handling
          if (remove.failed()) {
            future.fail("lookup failed");
            return;
          }
          future.complete("end");
        });
      }
    });
  }

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

    DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(
        new JsonObject().put("local", true));

    Vertx.vertx().deployVerticle(UsersWriteToMongo.class.getName(), options);
  }
}
