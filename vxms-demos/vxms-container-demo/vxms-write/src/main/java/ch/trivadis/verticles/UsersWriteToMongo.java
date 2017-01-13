package ch.trivadis.verticles;

import ch.trivadis.util.InitMongoDB;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.etcd.client.EtcdClient;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * Created by Andy Moncsek on 17.02.16.
 */
@EtcdClient(domain = "userAdmin", host = "etcd-client", port = 2379, ttl = 30, exportedHost = "write-verticle")
@ServiceEndpoint(name = "write-verticle", contextRoot = "/write", port = 8383)
public class UsersWriteToMongo extends VxmsEndpoint {
    private MongoClient mongo;


    @Override
    public void postConstruct(final Future<Void> startFuture) {
        mongo = InitMongoDB.initMongoData(vertx, config());
        startFuture.complete();
    }

    @Path("/api/users")
    @POST
    public void inertUser(RestHandler handler) {
        final Buffer body = handler.request().body();
        if (body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                response().
                stringResponse(future -> handleInsert(body, future)).
                retry(2).
                timeout(1000).
                onFailureRespond((onError, future) -> future.complete(onError.getMessage())).
                execute();
    }

    private void handleInsert(Buffer body, Future<String> future) {
        final JsonObject newUser = body.toJsonObject();
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

    @Path("/api/users/:id")
    @PUT
    public void updateUser(RestHandler handler) {
        final String id = handler.request().param("id");
        final Buffer body = handler.request().body();
        if (id == null || id.isEmpty() || body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                response().
                stringResponse(future -> handleUpdate(id, body, future)).
                retry(2).
                timeout(1000).
                onFailureRespond((onError, future) -> future.complete(onError.getMessage())).
                execute();
    }

    private void handleUpdate(String id, Buffer body, Future<String> future) {
        final JsonObject user = body.toJsonObject();
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
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                response().
                stringResponse(future -> handleDelete(id, future)).
                retry(2).
                timeout(1000).
                onFailureRespond((onError, future) -> future.complete(onError.getMessage())).
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

        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true).put("etcdport", 4001).put("etcdhost", "127.0.0.1").put("exportedHost", "localhost"));

        Vertx.vertx().deployVerticle(UsersWriteToMongo.class.getName(), options, handle -> {

        });

    }

}
