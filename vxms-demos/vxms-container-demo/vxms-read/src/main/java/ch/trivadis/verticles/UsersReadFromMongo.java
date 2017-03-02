package ch.trivadis.verticles;

import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.event.annotation.Consume;
import org.jacpfx.vertx.event.response.EventbusHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

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
                onError(error -> log.log(Level.ALL, "ERROR: " + error.getMessage())).
                onFailureRespond((failure, future) -> future.complete(DefaultResponses.
                        defaultErrorResponse(failure.getMessage()).
                        encodePrettily())).
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
                onError(error -> log.log(Level.ALL, "ERROR: " + error.getMessage())).
                onFailureRespond((failure, future) -> future.complete(DefaultResponses.
                        defaultErrorResponse(failure.getMessage()).
                        encodePrettily())).
                execute();
    }

}
