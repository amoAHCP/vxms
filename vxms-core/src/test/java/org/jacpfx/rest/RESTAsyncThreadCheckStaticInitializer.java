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
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
public class RESTAsyncThreadCheckStaticInitializer extends VertxTestBase {

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

    CountDownLatch latch2 = new CountDownLatch(1);

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

    client = getVertx().
        createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);

  }

  @Test

  public void simpleTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT).path("/wsService/simpleTest");
    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "test exception"));
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

  public void simpleRetryTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/simpleRetryTest");
    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "test exception"));
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

  public void simpleTimeoutTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
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

    latch.await();
    testComplete();

  }


  @Test

  public void simpleTimeoutWithRetryTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/simpleTimeoutWithRetryTest");
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

  public void statefulTimeoutWithRetryTest_() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/statefulTimeoutWithRetryTest/crash");
    target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

      @Override
      public void completed(String response) {
        System.out.println("Response entity '" + response + "' received.");
        vertx.runOnContext((e) -> assertEquals(response, "operation _timeout"));
        latch.countDown();
      }

      @Override
      public void failed(Throwable throwable) {
        throwable.printStackTrace();
      }
    });

    latch.await();
    testComplete();

  }

  @Test
  public void statefulTimeoutWithRetryTest() throws InterruptedException {
    Client client = ClientBuilder.newClient();

    //////// Request 1 -- creates crash
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/statefulTimeoutWithRetryTest/crash");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            vertx.runOnContext(h -> {
              System.out.println("--------");
              assertEquals("operation _timeout", response);

              //////// Request 1 -- is valid but crashes due to open circuit
              WebTarget target2 = client.target("http://" + HOST + ":" + PORT)
                  .path("/wsService/statefulTimeoutWithRetryTest/value");
              target2.request(MediaType.APPLICATION_JSON_TYPE).async()
                  .get(new InvocationCallback<String>() {

                    @Override
                    public void completed(String response) {
                      System.out.println("Response entity '" + response + "' received.");
                      vertx.runOnContext(h -> {
                        System.out.println("--------");
                        assertEquals("circuit open", response);

                        // wait 1s, but circuit is still open
                        vertx.setTimer(1205, handler -> {
                          target2.request(MediaType.APPLICATION_JSON_TYPE).async()
                              .get(new InvocationCallback<String>() {

                                @Override
                                public void completed(String response) {
                                  System.out
                                      .println("Response entity '" + response + "' received.");
                                  vertx.runOnContext(h -> {
                                    System.out.println("--------");
                                    assertEquals("circuit open", response);

                                    // wait another 1s, now circuit should be closed
                                    vertx.setTimer(2005, handler -> {
                                      target2.request(MediaType.APPLICATION_JSON_TYPE).async()
                                          .get(new InvocationCallback<String>() {

                                            @Override
                                            public void completed(String response) {
                                              System.out.println(
                                                  "Response entity '" + response + "' received.");
                                              vertx.runOnContext(h -> {
                                                System.out.println("--------");
                                                assertEquals("value", response);

                                                testComplete();


                                              });


                                            }

                                            @Override
                                            public void failed(Throwable throwable) {

                                            }
                                          });
                                    });


                                  });


                                }

                                @Override
                                public void failed(Throwable throwable) {

                                }
                              });
                        });


                      });


                    }

                    @Override
                    public void failed(Throwable throwable) {

                    }
                  });


            });
            // Assert.assertEquals(response, "test-123");

          }

          @Override
          public void failed(Throwable throwable) {

          }
        });

    await(80000, TimeUnit.MILLISECONDS);

  }


  public HttpClient getClient() {
    return client;
  }


  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne  extends AbstractVerticle {

    @Override
    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      VxmsEndpoint.start(startFuture,this);
    }

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
      System.out.println("stringResponse: " + reply);
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + val);
      reply.response().
          blocking().
          stringResponse(() -> {
            System.out.println("SLEEP");
              if (val.equals("crash")) {
                  Thread.sleep(6000);
              }
            System.out.println("SLEEP END");
            return val;
          }).
          timeout(1000).
          retry(3).
          closeCircuitBreaker(3000).
          onFailureRespond((error) -> error.getMessage()).
          execute();
    }

  }


}
