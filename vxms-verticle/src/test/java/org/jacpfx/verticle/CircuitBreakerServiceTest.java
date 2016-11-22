package org.jacpfx.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by amo on 22.11.16.
 */
@ServiceEndpoint(name = "CircuitBreakerServiceTest")
public class CircuitBreakerServiceTest extends VxmsEndpoint {

    @Path("/simpleTest")
    @GET
    public void simpleTest(RestHandler reply) {
        System.out.println("stringResponse: " + reply);
        reply.response().
                blocking().
                stringResponse(() -> {
                    throw new NullPointerException("test exception");
                }).
                onFailureRespond((error) -> error.getMessage()).
                execute();
    }

    @Path("/simpleRetryTest")
    @GET
    public void simpleRetryTest(RestHandler reply) {
        System.out.println("stringResponse: " + reply);
        reply.response().
                blocking().
                stringResponse(() -> {
                    throw new NullPointerException("test exception");
                }).
                retry(3).
                onFailureRespond((error) -> error.getMessage()).
                execute();
    }


    @Path("/simpleTimeoutTest")
    @GET
    public void simpleTimeoutTest(RestHandler reply) {
        System.out.println("stringResponse: " + reply);
        reply.response().
                blocking().
                stringResponse(() -> {
                    System.out.println("SLEEP");
                    Thread.sleep(8000);
                    System.out.println("SLEEP END");
                    return "reply";
                }).
                timeout(1000).
                onFailureRespond((error) -> error.getMessage()).
                execute();
    }


    @Path("/simpleTimeoutWithRetryTest")
    @GET
    public void simpleTimeoutWithRetryTest(RestHandler reply) {
        System.out.println("stringResponse: " + reply);
        reply.response().
                blocking().
                stringResponse(() -> {
                    System.out.println("SLEEP");
                    Thread.sleep(8000);
                    System.out.println("SLEEP END");
                    return "reply";
                }).
                timeout(1000).
                retry(3).
                onFailureRespond((error) -> error.getMessage()).
                execute();
    }

    @Path("/statefulTimeoutWithRetryTest/:val")
    @GET
    public void statefulTimeoutWithRetryTest(RestHandler reply) {
        final String val = reply.request().param("val");
        System.out.println("stringResponse: " + val);
        reply.response().
                blocking().
                stringResponse(() -> {
                    if (val.equals("crash")) Thread.sleep(6000);
                    return val;
                }).
                timeout(500).
                retry(3).
                closeCircuitBreaker(4000).
                onFailureRespond((error) -> {
                    System.out.println(error.getMessage());
                    return error.getMessage();
                }).
                execute();
    }


    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("host", "localhost"));
        Vertx.vertx().deployVerticle(CircuitBreakerServiceTest.class.getName(), options);
    }

}