package ch.trivadis.verticles;

import ch.trivadis.configuration.CustomEndpointConfig;
import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.vertx.etcd.client.EtcdClient;
import org.jacpfx.vertx.registry.DiscoveryClient;
import org.jacpfx.vertx.registry.Root;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.*;
import java.net.InetAddress;

/**
 * Created by Andy Moncsek on 01.04.16.
 * java -jar target/frontend-verticle-1.0-SNAPSHOT-fat.jar -conf local.json -cluster -cp cluster/
 */
@ServiceEndpoint(port = 8181, name = "gateway")
@EndpointConfig(CustomEndpointConfig.class)
@EtcdClient(domain = "userAdmin", host = "etcd-client", port = 2379, ttl = 30, exportedHost = "frontend-verticle", exportedPort = 80)
public class VxmsGateway extends VxmsEndpoint {


    private DiscoveryClient discoveryClient;


    @Override
    public void postConstruct(final Future<Void> startFuture) {
        // for demo purposes
        InitMongoDB.initMongoData(vertx, config());
        discoveryClient = DiscoveryClient.createClient(this);
        startFuture.complete();

    }


    @Path("/api/host1")
    @GET
    public void hostnameOne(RestHandler responseHandler) {
        responseHandler.response().stringResponse(future -> {
            vertx.createHttpClient().getAbs("http://etcd-client:2379/v2/keys/userAdmin/?recursive=true", handler -> handler.

                    bodyHandler(body -> future.complete(body.toString()))
            ).end();
        }).execute();
    }


    @Path("/api/users")
    @GET
    public void userGet(RestHandler handler) {
        handler.
                response().
                stringResponse(future ->
                        discoveryClient.
                                find("read-verticle").
                                onSuccess(node ->
                                        vertx.createHttpClient().getAbs(node.getServiceNode().getUri().toString() + "/api/users",
                                                resp -> resp.bodyHandler(body ->
                                                        future.complete(body.toString()))).end()).
                                onFailure(failureNode ->
                                        future.fail(failureNode.getThrowable())).
                                retry(2).
                                delay(500).
                                execute()).
                retry(2).
                timeout(2000).
                onFailureRespond((onError, future) -> future.complete(new JsonArray().add(DefaultResponses.defaultErrorResponse(onError.getMessage())).encodePrettily())).
                execute();
    }

    @Path("/api/users/:id")
    @GET
    public void userGetById(RestHandler handler) {
        final String id = handler.request().param("id");
        System.out.println("got ID: " + id);
        if (id == null || id.isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                response().
                stringResponse(future ->
                        discoveryClient.
                                find("read-verticle").
                                onSuccess(node ->
                                        vertx.createHttpClient().getAbs(node.getServiceNode().getUri().toString() + "/api/users/" + id,
                                                resp -> resp.bodyHandler(body ->
                                                        future.complete(body.toString()))).
                                                end()).
                                onFailure(failureNode ->
                                        future.fail(failureNode.getThrowable())).
                                retry(2).
                                delay(500).
                                execute()).
                retry(2).
                timeout(2000).
                onFailureRespond((onError, future) -> future.complete(DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily())).
                execute();
    }

    @Path("/api/users")
    @POST
    public void userPOST(RestHandler handler) {
        final Buffer body = handler.request().body();
        if (body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                response().
                stringResponse(future ->
                        discoveryClient.
                                find("write-verticle").
                                onSuccess(node -> {
                                    System.out.println("found node: "+node.getServiceNode().getUri().toString());
                                    vertx.createHttpClient().postAbs(node.getServiceNode().getUri().toString() + "/api/users",
                                            resp -> resp.bodyHandler(bodyHandler ->
                                                    future.complete(bodyHandler.toString()))).
                                            end(body);
                                }).
                                onFailure(failureNode ->
                                        future.fail(failureNode.getThrowable())).
                                retry(2).
                                delay(500).
                                execute()
                ).
                retry(2).
                timeout(2000).
                onFailureRespond((onError, future) -> future.complete(DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily())).
                execute();
    }

    @Path("/api/users/:id")
    @PUT
    public void userPutById(RestHandler handler) {
        final String id = handler.request().param("id");
        final Buffer body = handler.request().body();
        if (id == null || id.isEmpty() || body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        final JsonObject message = DefaultResponses.mapToUser(body.toJsonObject(), id);
        handler.
                response().
                stringResponse(future ->
                        discoveryClient.
                                find("write-verticle").
                                onSuccess(node ->
                                        vertx.createHttpClient().putAbs(node.getServiceNode().getUri().toString() + "/api/users/" + id,
                                                resp -> resp.bodyHandler(bodyHandler ->
                                                        future.complete(bodyHandler.toString()))).
                                                end(message.encode())).
                                onFailure(failureNode ->
                                        future.fail(failureNode.getThrowable())).
                                retry(2).
                                delay(500).
                                execute()).
                retry(2).
                timeout(2000).
                onFailureRespond((onError, future) -> future.complete(DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily())).
                execute();
    }

    @Path("/api/users/:id")
    @DELETE
    public void userDeleteById(RestHandler handler) {
        final String id = handler.request().param("id");
        if (id == null || id.isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                response().
                stringResponse(future ->
                        discoveryClient.
                                find("write-verticle").
                                onSuccess(node ->
                                        vertx.createHttpClient().putAbs(node.getServiceNode().getUri().toString() + "/api/users/" + id,
                                                resp -> resp.bodyHandler(bodyHandler ->
                                                        future.complete(bodyHandler.toString()))).
                                                end()).
                                onFailure(failureNode ->
                                        future.fail(failureNode.getThrowable())).
                                retry(2).
                                delay(500).
                                execute()).
                retry(2).
                timeout(2000).
                onFailureRespond((onError, future) -> future.complete(DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily())).
                execute(HttpResponseStatus.NO_CONTENT);

    }


    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).
                setConfig(new JsonObject().put("local", true).put("etcdport", 4001).put("etcdhost", "127.0.0.1").put("exportedHost", "localhost").put("exportedPort", 8181));

        Vertx.vertx().deployVerticle(VxmsGateway.class.getName(), options, handle -> {

        });

    }
}
