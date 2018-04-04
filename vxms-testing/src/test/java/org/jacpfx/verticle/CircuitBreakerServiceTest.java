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

package org.jacpfx.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/** Created by amo on 22.11.16. */
@ServiceEndpoint(name = "CircuitBreakerServiceTest")
public class CircuitBreakerServiceTest extends VxmsEndpoint {

  public static void main(String[] args) {
    DeploymentOptions options =
        new DeploymentOptions()
            .setInstances(2)
            .setConfig(new JsonObject().put("host", "localhost"));
    Vertx.vertx().deployVerticle(CircuitBreakerServiceTest.class.getName(), options);
  }

  public void postConstruct(Router router, final Future<Void> startFuture) {
    router
        .get("/test")
        .handler(
            handler -> {
              handler.response().end("hello");
            });
    startFuture.complete();
  }

  @Path("/simpleTest")
  @GET
  public void simpleTest(RestHandler reply) {
    System.out.println("stringResponse: " + reply);
    reply
        .response()
        .blocking()
        .stringResponse(
            () -> {
              throw new NullPointerException("test exception");
            })
        .onFailureRespond((error) -> error.getMessage())
        .execute();
  }

  @Path("/simpleRetryTest")
  @GET
  public void simpleRetryTest(RestHandler reply) {
    System.out.println("stringResponse: " + reply);
    reply
        .response()
        .blocking()
        .stringResponse(
            () -> {
              throw new NullPointerException("test exception");
            })
        .retry(3)
        .onFailureRespond((error) -> error.getMessage())
        .execute();
  }

  @Path("/simpleTimeoutTest")
  @GET
  public void simpleTimeoutTest(RestHandler reply) {
    System.out.println("stringResponse: " + reply);
    reply
        .response()
        .blocking()
        .stringResponse(
            () -> {
              System.out.println("SLEEP");
              Thread.sleep(8000);
              System.out.println("SLEEP END");
              return "reply";
            })
        .timeout(1000)
        .onFailureRespond((error) -> error.getMessage())
        .execute();
  }

  @Path("/simpleTimeoutWithRetryTest")
  @GET
  public void simpleTimeoutWithRetryTest(RestHandler reply) {
    System.out.println("stringResponse: " + reply);
    reply
        .response()
        .blocking()
        .stringResponse(
            () -> {
              System.out.println("SLEEP");
              Thread.sleep(8000);
              System.out.println("SLEEP END");
              return "reply";
            })
        .timeout(1000)
        .retry(3)
        .onFailureRespond((error) -> error.getMessage())
        .execute();
  }

  @Path("/statefulTimeoutWithRetryTest/:val")
  @GET
  public void statefulTimeoutWithRetryTest(RestHandler reply) {
    final String val = reply.request().param("val");
    System.out.println("stringResponse: " + val);
    reply
        .response()
        .blocking()
        .stringResponse(
            () -> {
              if (val.equals("crash")) {
                Thread.sleep(6000);
              }
              return val;
            })
        .timeout(500)
        .retry(3)
        .closeCircuitBreaker(4000)
        .onFailureRespond(
            (error) -> {
              System.out.println(error.getMessage());
              return error.getMessage();
            })
        .execute();
  }

  @Path("/statefulExceptionWithRetryTest/:val")
  @GET
  public void statefulExceptionWithRetryTest(RestHandler reply) {
    final String val = reply.request().param("val");
    System.out.println("stringResponse: " + val);
    reply
        .response()
        .blocking()
        .stringResponse(
            () -> {
              // System.out.println("+++stateless stringResponse: " + val + " equals: " +
              // val.equals("crash"));
              if (val.equals("crash")) {
                throw new NullPointerException("Test");
              }
              return val;
            })
        .timeout(500)
        .retry(3)
        .closeCircuitBreaker(4000)
        .onFailureRespond(
            (error) -> {
              //  System.out.println("---stateless stringResponse: " + val);
              //  System.out.println(error.getMessage());
              return error.getMessage();
            })
        .execute();
  }

  @Path("/statefulNonBlockingTimeoutWithRetryTest/:val")
  @GET
  public void statefulNonBlockingTimeoutWithRetryTest(RestHandler reply) {
    final String val = reply.request().param("val").trim();

    reply
        .response()
        .stringResponse(
            (future) -> {
              System.out.println(
                  "+++stateless stringResponse: " + val + " equals: " + val.equals("crash"));
              if (val.equals("crash")) {
                throw new NullPointerException("Test");
              }
              future.complete(val);
            })
        .timeout(500)
        .retry(3)
        .closeCircuitBreaker(1000)
        .onFailureRespond(
            (error, future) -> {
              // System.out.println("---stateless stringResponse: " + val);
              // System.out.println(error.getMessage());
              future.complete(error.getMessage());
            })
        .execute();
  }

  @Path("/statelessTimeoutWithRetryTest/:val")
  @GET
  public void statelessTimeoutWithRetryTest(RestHandler reply) {
    final String val = reply.request().param("val").trim();

    reply
        .response()
        .stringResponse(
            (future) -> {
              // System.out.println("+++stateless stringResponse: " + val + " equals: " +
              // val.equals("crash"));
              if (val.equals("crash")) {
                throw new NullPointerException("Test");
              }
              future.complete(val);
            })
        .timeout(500)
        .retry(3)
        .onFailureRespond(
            (error, future) -> {
              // System.out.println("---stateless stringResponse: " + val);
              // System.out.println(error.getMessage());
              future.complete(error.getMessage());
            })
        .execute();
  }

  @Path("/statelessTest/:val")
  @GET
  public void statelessTest(RestHandler reply) {
    final String val = reply.request().param("val").trim();
    reply
        .response()
        .stringResponse(
            (future) -> {
              // System.out.println("+++stateless stringResponse: " + val + " equals: " +
              // val.equals("crash"));
              future.complete(val);
            })
        .onFailureRespond(
            (error, future) -> {
              // System.out.println("---stateless stringResponse: " + val);
              // System.out.println(error.getMessage());
              future.complete(error.getMessage());
            })
        .execute();
  }
}
