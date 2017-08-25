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

package org.jacpfx.vxms.spa.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.vxms.spa.beans.UserLocalRepository;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.configuration.EndpointConfig;
import org.jacpfx.vxms.spa.config.CustomEndpointConfig;
import org.jacpfx.vxms.spa.config.CustomHTTPOptions;
import org.jacpfx.vxms.spa.config.SpringConfig;
import org.jacpfx.vxms.spa.util.DefaultResponses;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.jacpfx.vertx.spring.SpringVerticle;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Optional;

/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(port = 8443, host = "localhost", options = CustomHTTPOptions.class)
@EndpointConfig(CustomEndpointConfig.class)
@SpringVerticle(springConfig = SpringConfig.class)
public class SecureEndpoint extends VxmsEndpoint {


    @Inject
    private UserLocalRepository userLocalRepository;


    @Path("/private/api/users")
    @GET
    public void userGet(RestHandler handler) {
        // we use blocking here, since we assume a spring bean is doing some long running/blocking tasks
        handler.response().
                blocking().
                stringResponse(() -> userLocalRepository.getAll().encodePrettily()).
                onFailureRespond(error -> new JsonArray().add(DefaultResponses.defaultErrorResponse()).encodePrettily()).
                execute();
    }

    @Path("/private/api/users/:id")
    @GET
    public void userGetById(RestHandler handler) {
        final String id = handler.request().param("id");
        // we use blocking here, since we assume a spring bean is doing some long running/blocking tasks
        handler.response().
                blocking().
                stringResponse(() -> userLocalRepository.getUserById(id).encodePrettily()).
                onFailureRespond(error -> DefaultResponses.defaultErrorResponse().encodePrettily()).
                execute();
    }

    @Path("/private/api/users")
    @POST
    public void userPOST(RestHandler handler) {
        final Buffer body = handler.request().body();
        // we use blocking here, since we assume a spring bean is doing some long running/blocking tasks
        handler.response().
                blocking().
                stringResponse(() -> userLocalRepository.addUser(body.toJsonObject()).encodePrettily()).
                onFailureRespond(error -> DefaultResponses.defaultErrorResponse().encodePrettily()).
                execute();
    }

    @Path("/private/api/users/:id")
    @PUT
    public void userPutById(RestHandler handler) {
        final String id = handler.request().param("id");
        final Buffer body = handler.request().body();
        final JsonObject message = DefaultResponses.mapToUser(body.toJsonObject(), id);
        // we use blocking here, since we assume a spring bean is doing some long running/blocking tasks
        handler.response().
                blocking().
                stringResponse(() -> userLocalRepository.updateUser(message).encodePrettily()).
                onFailureRespond(error -> DefaultResponses.defaultErrorResponse().encodePrettily()).
                execute();
    }

    @Path("/private/api/users/:id")
    @DELETE
    public void userDeleteById(RestHandler handler) {
        final String id = handler.request().param("id");
        // we use blocking here, since we assume a spring bean is doing some long running/blocking tasks
        handler.response().
                blocking().
                stringResponse(() -> {
                    userLocalRepository.deleteUser(id);
                    return "ok";
                }).execute();


    }

    @Path("/private/userinfo")
    @GET
    public void userInfo(RestHandler handler) {
        Optional.ofNullable(handler.context().user()).ifPresent(user -> {
            final JsonObject principal = user.principal();
            final String token = principal.getString("access_token");
            handler.
                    response().
                    stringResponse((response) -> requestUser(token, response)).
                    timeout(2000).
                    onFailureRespond((error, response) -> response.complete("no data")).
                    execute();

        });
    }

    public void requestUser(String token, Future<String> response) {
        vertx.createHttpClient(OPTIONS).getAbs("https://api.github.com/user?access_token=" + token, responseHandler ->
                responseHandler.handler(body -> response.complete(getUsername(body)))).
                putHeader("User-Agent", "demo-app").
                end();
    }

    protected String getUsername(Buffer body) {
        String json = body.toString();
        JsonObject entries = new JsonObject(json);
        return entries.getString("name");
    }


    /**
     * for local testing
     *
     * @param args
     */
    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("host", "localhost").put("clientID", "xx").put("clientSecret", "xx"));
        Vertx.vertx().deployVerticle("java-spring:" + SecureEndpoint.class.getName(), options);


    }

    public static final HttpClientOptions OPTIONS = new HttpClientOptions().
            setLogActivity(true).
            setSsl(true).
            setTrustAll(true);
}
