package org.jacpfx.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.beans.UserLocalRepository;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.config.CustomEndpointConfig;
import org.jacpfx.config.CustomHTTPOptions;
import org.jacpfx.config.SpringConfig;
import org.jacpfx.util.DefaultResponses;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
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
    public static final HttpClientOptions OPTIONS = new HttpClientOptions().
            setLogActivity(true).
            setSsl(true).
            setTrustAll(true);


    @Path("/private/userinfo")
    @GET
    public void userInfo(RestHandler handler) {
        Optional.ofNullable(handler.context().user()).ifPresent(user -> {
            final JsonObject principal = user.principal();
            final String token = principal.getString("access_token");
            vertx.createHttpClient(OPTIONS).getAbs("https://api.github.com/user?access_token=" + token, responseHandler ->
                    responseHandler.handler(body ->
                            handler.response().stringResponse(() -> getUsername(body)).execute())).
                    putHeader("User-Agent", "demo-app").
                    end();
        });
    }

    protected String getUsername(Buffer body) {
        String json = body.toString();
        JsonObject entries = new JsonObject(json);
        return entries.getString("name");
    }

    @Path("/private/api/users")
    @GET
    public void userGet(RestHandler handler) {
        handler.response().
                stringResponse(() -> userLocalRepository.getAll().encodePrettily()).
                onFailureRespond(error -> new JsonArray().add(DefaultResponses.defaultErrorResponse()).encodePrettily()).
                execute();
    }

    @Path("/private/api/users/:id")
    @GET
    public void userGetById(RestHandler handler) {
        final String id = handler.request().param("id");
        handler.response().stringResponse(() -> userLocalRepository.getUserById(id).encodePrettily()).
                onFailureRespond(error -> DefaultResponses.defaultErrorResponse().encodePrettily()).
                execute();
    }

    @Path("/private/api/users")
    @POST
    public void userPOST(RestHandler handler) {
        final Buffer body = handler.request().body();
        handler.response().stringResponse(() -> userLocalRepository.addUser(body.toJsonObject()).encodePrettily()).
                onFailureRespond(error -> DefaultResponses.defaultErrorResponse().encodePrettily()).
                execute();
    }

    @Path("/private/api/users/:id")
    @PUT
    public void userPutById(RestHandler handler) {
        final String id = handler.request().param("id");
        final Buffer body = handler.request().body();
        final JsonObject message = DefaultResponses.mapToUser(body.toJsonObject(), id);
        handler.response().stringResponse(() -> userLocalRepository.updateUser(message).encodePrettily()).
                onFailureRespond(error -> DefaultResponses.defaultErrorResponse().encodePrettily()).
                execute();
    }

    @Path("/private/api/users/:id")
    @DELETE
    public void userDeleteById(RestHandler handler) {
        final String id = handler.request().param("id");
        handler.response().stringResponse(() -> {
            userLocalRepository.deleteUser(id);
            return "ok";
        }).execute();


    }


    /**
     * for local testing
     *
     * @param args
     */
    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("host", "localhost").put("clientID", "xxx").put("clientSecret", "xxx"));
        Vertx.vertx().deployVerticle("java-spring:" + SecureEndpoint.class.getName(), options);


    }
}
