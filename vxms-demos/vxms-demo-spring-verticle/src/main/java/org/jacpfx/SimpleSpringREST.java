package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jacpfx.beans.HelloWorldBean;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.configuration.SpringConfig;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.jacpfx.vertx.spring.SpringVerticle;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(value = "/", port = 9090)
@SpringVerticle(springConfig = SpringConfig.class)
public class SimpleSpringREST extends VxmsEndpoint {
    @Inject
    HelloWorldBean bean;

    @Path("helloGET")
    @GET
    public void simpleRESTHello(RestHandler handler) {
        handler.response().stringResponse(() -> bean.sayHallo()).execute();
    }


    @Path("helloGET/:name")
    @GET
    public void simpleRESTHelloWithParameter(RestHandler handler) {
        handler.response().async().stringResponse(() -> bean.sayHallo(handler.request().param("name"))).execute();
    }

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("host", "localhost"));
        Vertx.vertx().deployVerticle("java-spring:" + SimpleSpringREST.class.getName(), options);
    }
}
