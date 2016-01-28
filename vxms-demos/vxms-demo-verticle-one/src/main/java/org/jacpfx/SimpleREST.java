package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(value = "/", port = 9090)
public class SimpleREST extends VxmsEndpoint {

    @Path("helloGET")
    @GET
    public void simpleRESTHello(RestHandler handler) {
         handler.response().stringResponse(()->"hello World").execute();
    }


    @Path("helloGET/:name")
    @GET
    public void simpleRESTHelloWithParameter(RestHandler handler) {
        handler.response().stringResponse(()->"hello World "+handler.request().param("name")).execute();
    }

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("host","localhost"));
        Vertx.vertx().deployVerticle(SimpleREST.class.getName(),options);
    }
}
