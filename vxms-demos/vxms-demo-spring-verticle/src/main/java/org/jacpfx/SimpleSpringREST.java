package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jacpfx.beans.HelloWorldBean;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.configuration.SpringConfig;
import org.jacpfx.vertx.rest.annotation.OnRestError;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.jacpfx.vertx.spring.SpringVerticle;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(port = 9090)
@SpringVerticle(springConfig = SpringConfig.class)
public class SimpleSpringREST extends VxmsEndpoint {

    @Inject
    HelloWorldBean bean;

    @Path("/helloGET")
    @GET
    public void simpleRESTHello(RestHandler handler) {
        handler.response().stringResponse(() -> bean.sayHallo()+"  ..... 1").execute();
    }


    @Path("/helloGET/:name")
    @GET
    public void simpleRESTHelloWithParameter(RestHandler handler) {
        handler.response().blocking().stringResponse(() -> {
            final String name = handler.request().param("name");
            return bean.sayHallo(name);
        }).execute();
    }


    @Path("/simpleExceptionHandling/:name")
    @GET
    public void simpleExceptionHandling(RestHandler handler) {
        handler.response().blocking().stringResponse(() -> bean.seyHelloWithException()).execute();
    }

    @OnRestError("/simpleExceptionHandling/:name")
    @GET
    public void simpleExceptionHandlingOnError(Throwable t, RestHandler handler) {
        //System.out.println("ERROR");
        handler.response().stringResponse(() -> bean.sayHallo(handler.request().param("name")+" ::"+t.getMessage())).execute();
    }

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1000).setConfig(new JsonObject().put("host", "localhost"));
        Vertx.vertx().deployVerticle("java-spring:" + SimpleSpringREST.class.getName(), options);
    }
}
