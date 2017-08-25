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

package org.jacpfx.vxms.write.verticles;

import org.jacpfx.vxms.write.util.DefaultResponses;
import org.jacpfx.vxms.write.util.InitMongoDB;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.event.annotation.Consume;
import org.jacpfx.vxms.event.response.EventbusHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Andy Moncsek on 17.02.16.
 */
@ServiceEndpoint(name = "write-verticle", contextRoot = "/write", port = 0)
public class UsersWriteToMongo extends VxmsEndpoint {
    Logger log = Logger.getLogger(UsersWriteToMongo.class.getName());
    private MongoClient mongo;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true).put("etcdport", 4001).put("etcdhost", "127.0.0.1").put("exportedHost", "localhost"));

        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(UsersWriteToMongo.class.getName(), options, handle -> {

                });
            }
        });
    }

    @Override
    public void postConstruct(final Future<Void> startFuture) {
        mongo = InitMongoDB.initMongoData(vertx, config());
        startFuture.complete();
    }

    @Consume("/api/users-POST")
    public void inertUser(EventbusHandler handler) {
        final JsonObject body = handler.request().body();
        if (body == null || body.isEmpty()) {
            handler.response().stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse("no content").encode())).execute();
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
        mongo.findOne("users", new JsonObject().put("username", newUser.getString("username")), null, lookup -> {
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

    @Consume("/api/users/:id-PUT")
    public void updateUser(EventbusHandler handler) {
        final JsonObject user = handler.request().body();
        if (user == null || user.isEmpty()) {
            handler.response().stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse().encode())).execute();
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

    @Consume("/api/users/:id-DELETE")
    public void deleteUser(EventbusHandler handler) {
        final String id = handler.request().body();
        if (id == null || id.isEmpty()) {
            handler.response().stringResponse(f -> f.fail(DefaultResponses.defaultErrorResponse().encode())).execute();
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

}
