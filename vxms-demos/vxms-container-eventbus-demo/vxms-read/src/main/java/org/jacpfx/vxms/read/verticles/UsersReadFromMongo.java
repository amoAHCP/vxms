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

package org.jacpfx.vxms.read.verticles;

import org.jacpfx.vxms.read.util.DefaultResponses;
import org.jacpfx.vxms.read.util.InitMongoDB;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.event.annotation.Consume;
import org.jacpfx.vxms.event.response.EventbusHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Andy Moncsek on 17.02.16.
 */
@ServiceEndpoint(name = "read-verticle", contextRoot = "/read", port = 0)
public class UsersReadFromMongo extends VxmsEndpoint {
    Logger log = Logger.getLogger(UsersReadFromMongo.class.getName());
    private MongoClient mongo;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true).put("etcdport", 4001).put("etcdhost", "127.0.0.1").put("exportedHost", "localhost"));

        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(UsersReadFromMongo.class.getName(), options, handle -> {

                });
            }
        });

    }

    @Override
    public void postConstruct(final Future<Void> startFuture) {
        mongo = InitMongoDB.initMongoData(vertx, config());
        startFuture.complete();
    }

    @Consume("/api/users-GET")
    public void getAllUsers(EventbusHandler reply) {
        reply.
                response().
                stringResponse((future) -> mongo.find("users", new JsonObject(), lookup -> {
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

                })).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((failure, future) ->
                        future.complete(DefaultResponses.
                                defaultErrorResponse(failure.getMessage()).
                                encodePrettily())
                ).
                execute();
    }

    @Consume("/api/users/:id-GET")
    public void getUserById(EventbusHandler reply) {
        String id = reply.request().body();
        reply.
                response().
                stringResponse((future) -> mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
                    // error handling
                    if (lookup.failed()) {
                        future.fail(lookup.cause());
                    } else if (lookup.result() != null) {
                        future.complete(lookup.result().encode());
                    } else {
                        future.fail("no user found");
                    }

                })).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((failure, future) ->
                        future.complete(DefaultResponses.
                                defaultErrorResponse(failure.getMessage()).
                                encodePrettily())
                ).
                execute();
    }

}
