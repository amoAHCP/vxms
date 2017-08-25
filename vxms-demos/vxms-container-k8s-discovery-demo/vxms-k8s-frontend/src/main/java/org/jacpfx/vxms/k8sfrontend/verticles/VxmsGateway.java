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

package org.jacpfx.vxms.k8sfrontend.verticles;

import org.jacpfx.vxms.k8sfrontend.configuration.CustomEndpointConfig;
import org.jacpfx.vxms.k8sfrontend.util.DefaultResponses;
import org.jacpfx.vxms.k8sfrontend.util.InitMongoDB;
import io.fabric8.annotations.ServiceName;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.jacpfx.client.Fabric8DiscoveryClient;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.configuration.EndpointConfig;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/**
 * Created by Andy Moncsek on 11.05.17.
 */
@ServiceEndpoint(port = 8181, name = "vxms-frontend")
@EndpointConfig(CustomEndpointConfig.class)
public class VxmsGateway extends VxmsEndpoint {


  @ServiceName("vxms-k8s-read")
  private String read;

  @ServiceName("vxms-k8s-write")
  private String write;


  Logger log = Logger.getLogger(VxmsGateway.class.getName());

  @Override
  public void postConstruct(final Future<Void> startFuture) {
    // for demo purposes
    InitMongoDB.initMongoData(vertx, config());
    // init service discovery
    Fabric8DiscoveryClient.
        builder().
        apiToken(null).
        masterUrl(Fabric8DiscoveryClient.DEFAULT_MASTER_URL).
        namespace(Fabric8DiscoveryClient.DEFAULT_NAMESPACE).
        resolveAnnotations(this);

    startFuture.complete();

  }

  @Path("/api/users")
  @GET
  public void userGet(RestHandler handler) {
    handler.
        response().
        stringResponse(this::requestAllUsers).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(new JsonArray().add(DefaultResponses.
                defaultErrorResponse(onError.getMessage())).
                encodePrettily())
        ).
        execute();
  }

  public void requestAllUsers(Future<String> future) {
    vertx.createHttpClient().getAbs("http://" + read + "/read/api/users", resp -> {
      writeResponse(future, resp);
      handleRequestError(future, resp);
    }).end();
  }


  @Path("/api/users/:id")
  @GET
  public void userGetById(RestHandler handler) {
    final String id = handler.request().param("id");
    if (id == null || id.isEmpty()) {
      handler.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    handler.
        response().
        stringResponse(future -> requestUserById(id, future)).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encodePrettily())
        ).
        execute();
  }

  public void requestUserById(String id, Future<String> future) {
    vertx.createHttpClient().getAbs("http://" + read + "/read/api/users/" + id, resp -> {
      writeResponse(future, resp);
      handleRequestError(future, resp);
    }).end();
  }


  public void writeResponse(Future<String> future, HttpClientResponse resp) {
    resp.bodyHandler(body -> {
      String val = body.getString(0, body.length());
      future.complete(val);
    });
  }

  public void handleRequestError(Future<String> future, HttpClientResponse resp) {
    resp.exceptionHandler(fail -> future.fail(fail));
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
        stringResponse(future -> insertUser(body, future)).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encodePrettily())
        ).
        httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
        execute();
  }

  public void insertUser(Buffer body, Future<String> future) {
    vertx.createHttpClient().postAbs("http://" + write + "/write/api/users", resp -> {
      writeResponse(future, resp);
      handleRequestError(future, resp);
    }).end(body);
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
    final JsonObject user = DefaultResponses.mapToUser(body.toJsonObject(), id);
    handler.
        response().
        stringResponse(future -> updateUser(user, future)).
        retry(2).
        timeout(2000).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encodePrettily())
        ).
        httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
        execute();
  }

  public void updateUser(JsonObject user, Future<String> future) {
    vertx.createHttpClient().putAbs("http://" + write + "/write/api/users", resp -> {
      writeResponse(future, resp);
      handleRequestError(future, resp);
    }).end(Buffer.buffer(user.encode()));
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
        stringResponse(future -> deleteUser(id, future)).
        retry(2).
        timeout(2000).
        onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
        onFailureRespond((onError, future) ->
            future.complete(DefaultResponses.
                defaultErrorResponse(onError.getMessage()).
                encodePrettily())
        ).
        httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
        execute(HttpResponseStatus.NO_CONTENT);

  }

  public void deleteUser(String id, Future<String> future) {
    vertx.createHttpClient()
        .deleteAbs("http://" + read + "/write/api/users/" + id, resp -> {
          writeResponse(future, resp);
          handleRequestError(future, resp);
        }).end();
  }


  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(
        new JsonObject().put("local", true));

    Vertx.vertx().deployVerticle(VxmsGateway.class.getName(),options);
  }
}
