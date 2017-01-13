package ch.trivadis.verticles;

import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.etcd.client.EtcdClient;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.stream.Collectors;

/**
 * Created by Andy Moncsek on 17.02.16.
 */
@EtcdClient(domain = "userAdmin", host = "etcd-client", port = 2379, ttl = 30, exportedHost = "read-verticle")
@ServiceEndpoint(name = "read-verticle", contextRoot = "/read", port = 8282)
public class UsersReadFromMongo extends VxmsEndpoint {
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
                stringResponse((future) -> mongo.find("users", new JsonObject(), lookup -> {
                    // error handling
                    if (lookup.failed()) {
                        future.fail(lookup.cause());
                    } else {
                        future.complete(new JsonArray(lookup.result().stream().collect(Collectors.toList())).encode());
                    }

                })).
                timeout(2000).
                onFailureRespond((failure, future) -> future.complete(new JsonArray().add(DefaultResponses.defaultErrorResponse()).encode())).
                execute();
    }

    @Path("/api/users/:id")
    @GET
    public void getUserById(RestHandler reply) {
        String id = reply.request().param("id");
        reply.
                response().
                stringResponse((future) -> mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
                    // error handling
                    if (lookup.failed()) {
                        future.fail(lookup.cause());
                    } else if(lookup.result()!=null) {
                        future.complete(lookup.result().encode());
                    } else {
                        future.fail("no user found");
                    }

                })).
                timeout(2000).
                onFailureRespond((failure, future) -> future.complete(DefaultResponses.defaultErrorResponse().encode())).
                execute();
    }





    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true).put("etcdport",4001).put("etcdhost","127.0.0.1").put("exportedHost","localhost"));

        Vertx.vertx().deployVerticle(UsersReadFromMongo.class.getName(), options, handle -> {

        });

    }

}
