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

package org.jacpfx.rest;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientTimeoutTests extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private final static int MAX_RESPONSE_ELEMENTS = 4;
  private static final String HOST = "127.0.0.1";
  private HttpServer http;
  private HttpClient client;

  protected int getNumNodes() {
    return 1;
  }

  protected Vertx getVertx() {
    return vertices[0];
  }

  @Override
  protected ClusterManager getClusterManager() {
    return new FakeClusterManager();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    startNodes(getNumNodes());

  }

  @Before
  public void startVerticles() throws InterruptedException {

    CountDownLatch latch2 = new CountDownLatch(2);

    DeploymentOptions options = new DeploymentOptions().setInstances(1);
    options.setConfig(new JsonObject().put("clustered", false).put("host", HOST)).setInstances(1);
    // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
    // don'failure have to hardecode it in your tests

    getVertx().deployVerticle(new WsServiceOne(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });

    getVertx().deployVerticle(new TestVerticle(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });

    client = getVertx().
        createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);

  }


  @Test

  public void simpleTimeoutTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/simpleTimeoutTest");
    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "operation _timeout"));
        latch.countDown();
      }

      @Override
      public void failed(Throwable throwable) {

      }
    });

    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "operation _timeout"));
        latch.countDown();
      }

      @Override
      public void failed(Throwable throwable) {

      }
    });

    latch.await();
    testComplete();

  }

  @Test

  public void simpleTimeoutNonBlockingTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/simpleTimeoutNonBlockingTest");
    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "failure"));
        latch.countDown();
      }

      @Override
      public void failed(Throwable throwable) {

      }
    });

    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "failure"));
        latch.countDown();
      }

      @Override
      public void failed(Throwable throwable) {

      }
    });

    latch.await();
    testComplete();

  }

  @Test

  public void eventbusTimeoutNonBlockingTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/eventbusTimeoutNonBlockingTest");
    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "timeout"));
        latch.countDown();
      }

      @Override
      public void failed(Throwable throwable) {

      }
    });

    latch.await();
    testComplete();

  }


  public HttpClient getClient() {
    return client;
  }


  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

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

    @Path("/simpleTimeoutNonBlockingTest")
    @GET
    public void simpleTimeoutNonBlockingTest(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().
          stringResponse((future) -> {
            getVertx().
                createHttpClient(new HttpClientOptions())
                .getNow(PORT, HOST, SERVICE_REST_GET + "/long", response -> {
                    if (!future.isComplete()) {
                        future.complete("reply");
                    }
                });

          }).
          timeout(1000).
          onError(e -> {
            System.out.println("TIMEOUT");
          }).
          retry(3).
          onFailureRespond((error, response) -> {
            System.out.println("ON FAILURE");
            response.complete("failure");
          }).
          execute();
    }


    @Path("/eventbusTimeoutNonBlockingTest")
    @GET
    public void eventbusTimeoutNonBlockingTest(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.eventBusRequest().send("hello", "payload", new DeliveryOptions().setSendTimeout(500)).
          mapToStringResponse((message, future) -> {
            System.out.println("CAUSE: " + message.cause());
            future.complete(message.result().body().toString());
          }).
          retry(2).
          onFailureRespond((t, c) -> {
            System.out.println("TIMEOUT ERROR" + t.getMessage());
            c.complete("timeout");
          }).

          execute();
    }


    @Path("/long")
    @GET
    public void longRunner(RestHandler reply) {
      System.out.println("stringResponse ---: " + reply);
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }


  }

  public class TestVerticle extends AbstractVerticle {

    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      System.out.println("start");
      vertx.eventBus().consumer("hello", handler -> {
        vertx.executeBlocking(blocking -> {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println("request::" + handler.body().toString());
          handler.reply("hello");
        }, result -> {
        });

      });
      startFuture.complete();
    }
  }

}
