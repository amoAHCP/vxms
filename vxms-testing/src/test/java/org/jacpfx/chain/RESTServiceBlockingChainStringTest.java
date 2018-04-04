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

package org.jacpfx.chain;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTServiceBlockingChainStringTest extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private static final int MAX_RESPONSE_ELEMENTS = 4;
  private static final String HOST = "127.0.0.1";
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
    options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
    // Deploy the module - the System property `vertx.modulename` will contain the name of the
    // module so you
    // don'failure have to hardecode it in your tests

    getVertx()
        .deployVerticle(
            new WsServiceOne(),
            options,
            asyncResult -> {
              // Deployment is asynchronous and this this handler will be called when it's complete
              // (or failed)
              System.out.println("start service: " + asyncResult.succeeded());
              assertTrue(asyncResult.succeeded());
              assertNotNull("deploymentID should not be null", asyncResult.result());
              // If deployed correctly then start the tests!
              //   latch2.countDown();

              latch2.countDown();
            });

    client = getVertx().createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);
  }

  @Test
  public void basicTestSupply() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestSupply",
            resp ->
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "1 final");
                      testComplete();
                    }));
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestAndThen() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestAndThen",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "2 final");
                      testComplete();
                    });
              }
            });
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestSupplyWithError() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestSupplyWithError",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "error test error");
                      testComplete();
                    });
              }
            });
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestAndThenWithError() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestAndThenWithError",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "error test error");
                      testComplete();
                    });
              }
            });
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestSupplyWithErrorUnhandled() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestSupplyWithErrorUnhandled",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                Assert.assertEquals(resp.statusCode(), 500);
                Assert.assertEquals(resp.statusMessage(), "test error");
                testComplete();
              }
            });
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestAndThenWithErrorUnhandled() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestAndThenWithErrorUnhandled",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                Assert.assertEquals(resp.statusCode(), 500);
                Assert.assertEquals(resp.statusMessage(), "test error");
                testComplete();
              }
            });
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestSupplyWithErrorSimpleRetry() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestSupplyWithErrorSimpleRetry",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "error 4 test error");
                      testComplete();
                    });
              }
            });
    request.end();
    await();
  }

  @Test
  // @Ignore
  public void basicTestAndThenWithErrorUnhandledRetry() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/basicTestAndThenWithErrorUnhandledRetry",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "error 4 test error");
                      testComplete();
                    });
              }
            });
    request.end();
    await();
  }

  @Test
  public void basicTestSupplyWithErrorAndCircuitBreaker() throws InterruptedException {
    Client client = ClientBuilder.newClient();

    //////// Request 1 -- creates crash
    WebTarget target =
        client
            .target("http://" + HOST + ":" + PORT)
            .path("/wsService/basicTestSupplyWithErrorAndCircuitBreaker/crash");
    Future<String> getCallback =
        target
            .request(MediaType.APPLICATION_JSON_TYPE)
            .async()
            .get(
                new InvocationCallback<String>() {

                  @Override
                  public void completed(String response) {
                    System.out.println("Response entity '" + response + "' received.");
                    vertx.runOnContext(
                        h -> {
                          System.out.println("--------");
                          assertEquals("failure", response);

                          //////// Request 1 -- is valid but crashes due to open circuit
                          WebTarget target2 =
                              client
                                  .target("http://" + HOST + ":" + PORT)
                                  .path(
                                      "/wsService/basicTestSupplyWithErrorAndCircuitBreaker/value");
                          target2
                              .request(MediaType.APPLICATION_JSON_TYPE)
                              .async()
                              .get(
                                  new InvocationCallback<String>() {

                                    @Override
                                    public void completed(String response) {
                                      System.out.println(
                                          "Response entity '" + response + "' received.");
                                      vertx.runOnContext(
                                          h -> {
                                            System.out.println("--------");
                                            assertEquals("failure", response);

                                            // wait 1s, but circuit is still open
                                            vertx.setTimer(
                                                1205,
                                                handler -> {
                                                  target2
                                                      .request(MediaType.APPLICATION_JSON_TYPE)
                                                      .async()
                                                      .get(
                                                          new InvocationCallback<String>() {

                                                            @Override
                                                            public void completed(String response) {
                                                              System.out.println(
                                                                  "Response entity '"
                                                                      + response
                                                                      + "' received.");
                                                              vertx.runOnContext(
                                                                  h -> {
                                                                    System.out.println("--------");
                                                                    assertEquals(
                                                                        "failure", response);

                                                                    // wait another 1s, now circuit
                                                                    // should be closed
                                                                    vertx.setTimer(
                                                                        1005,
                                                                        handler -> {
                                                                          target2
                                                                              .request(
                                                                                  MediaType
                                                                                      .APPLICATION_JSON_TYPE)
                                                                              .async()
                                                                              .get(
                                                                                  new InvocationCallback<
                                                                                      String>() {

                                                                                    @Override
                                                                                    public void
                                                                                        completed(
                                                                                            String
                                                                                                response) {
                                                                                      System.out
                                                                                          .println(
                                                                                              "Response entity '"
                                                                                                  + response
                                                                                                  + "' received.");
                                                                                      vertx
                                                                                          .runOnContext(
                                                                                              h -> {
                                                                                                System
                                                                                                    .out
                                                                                                    .println(
                                                                                                        "--------");
                                                                                                assertEquals(
                                                                                                    "value",
                                                                                                    response);

                                                                                                testComplete();
                                                                                              });
                                                                                    }

                                                                                    @Override
                                                                                    public void
                                                                                        failed(
                                                                                            Throwable
                                                                                                throwable) {}
                                                                                  });
                                                                        });
                                                                  });
                                                            }

                                                            @Override
                                                            public void failed(
                                                                Throwable throwable) {}
                                                          });
                                                });
                                          });
                                    }

                                    @Override
                                    public void failed(Throwable throwable) {}
                                  });
                        });
                    // Assert.assertEquals(response, "test-123");

                  }

                  @Override
                  public void failed(Throwable throwable) {}
                });

    await(6000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void basicTestAndThenWithErrorAndCircuitBreaker() throws InterruptedException {
    Client client = ClientBuilder.newClient();

    //////// Request 1 -- creates crash
    WebTarget target =
        client
            .target("http://" + HOST + ":" + PORT)
            .path("/wsService/basicTestAndThenWithErrorAndCircuitBreaker/crash");
    Future<String> getCallback =
        target
            .request(MediaType.APPLICATION_JSON_TYPE)
            .async()
            .get(
                new InvocationCallback<String>() {

                  @Override
                  public void completed(String response) {
                    System.out.println("Response entity '" + response + "' received.");
                    vertx.runOnContext(
                        h -> {
                          System.out.println("--------");
                          assertEquals("failure", response);

                          //////// Request 1 -- is valid but crashes due to open circuit
                          WebTarget target2 =
                              client
                                  .target("http://" + HOST + ":" + PORT)
                                  .path(
                                      "/wsService/basicTestAndThenWithErrorAndCircuitBreaker/value");
                          target2
                              .request(MediaType.APPLICATION_JSON_TYPE)
                              .async()
                              .get(
                                  new InvocationCallback<String>() {

                                    @Override
                                    public void completed(String response) {
                                      System.out.println(
                                          "Response entity '" + response + "' received.");
                                      vertx.runOnContext(
                                          h -> {
                                            System.out.println("--------");
                                            assertEquals("failure", response);

                                            // wait 1s, but circuit is still open
                                            vertx.setTimer(
                                                1205,
                                                handler -> {
                                                  target2
                                                      .request(MediaType.APPLICATION_JSON_TYPE)
                                                      .async()
                                                      .get(
                                                          new InvocationCallback<String>() {

                                                            @Override
                                                            public void completed(String response) {
                                                              System.out.println(
                                                                  "Response entity '"
                                                                      + response
                                                                      + "' received.");
                                                              vertx.runOnContext(
                                                                  h -> {
                                                                    System.out.println("--------");
                                                                    assertEquals(
                                                                        "failure", response);

                                                                    // wait another 1s, now circuit
                                                                    // should be closed
                                                                    vertx.setTimer(
                                                                        1005,
                                                                        handler -> {
                                                                          target2
                                                                              .request(
                                                                                  MediaType
                                                                                      .APPLICATION_JSON_TYPE)
                                                                              .async()
                                                                              .get(
                                                                                  new InvocationCallback<
                                                                                      String>() {

                                                                                    @Override
                                                                                    public void
                                                                                        completed(
                                                                                            String
                                                                                                response) {
                                                                                      System.out
                                                                                          .println(
                                                                                              "Response entity '"
                                                                                                  + response
                                                                                                  + "' received.");
                                                                                      vertx
                                                                                          .runOnContext(
                                                                                              h -> {
                                                                                                System
                                                                                                    .out
                                                                                                    .println(
                                                                                                        "--------");
                                                                                                assertEquals(
                                                                                                    "value",
                                                                                                    response);

                                                                                                testComplete();
                                                                                              });
                                                                                    }

                                                                                    @Override
                                                                                    public void
                                                                                        failed(
                                                                                            Throwable
                                                                                                throwable) {}
                                                                                  });
                                                                        });
                                                                  });
                                                            }

                                                            @Override
                                                            public void failed(
                                                                Throwable throwable) {}
                                                          });
                                                });
                                          });
                                    }

                                    @Override
                                    public void failed(Throwable throwable) {}
                                  });
                        });
                    // Assert.assertEquals(response, "test-123");

                  }

                  @Override
                  public void failed(Throwable throwable) {}
                });

    await(6000, TimeUnit.MILLISECONDS);
  }

  @Test
  // @Ignore
  public void endpointOne() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointOne",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());
                      Assert.assertEquals(body.toString(), "1test final");
                      testComplete();
                    });
              }
            });
    request.end();
    await();
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Path("/basicTestSupply")
    @GET
    public void basicTestSupply(RestHandler reply) {
      System.out.println("basicTestSupply: " + reply);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                return 1;
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .execute();
    }

    @Path("/basicTestAndThen")
    @GET
    public void basicTestAndThen(RestHandler reply) {
      System.out.println("basicTestAndThen: " + reply);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                return 1;
              })
          .andThen(
              (value) -> {
                return value + 1 + "";
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .execute();
    }

    @Path("/basicTestSupplyWithError")
    @GET
    public void basicTestSupplyWithError(RestHandler reply) {
      System.out.println("basicTestSupplyWithError: " + reply);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .onFailureRespond(
              (t) -> {
                System.out.println("error: " + t.getMessage());
                return "error " + t.getMessage();
              })
          .execute();
    }

    @Path("/basicTestAndThenWithError")
    @GET
    public void basicTestAndThenWithError(RestHandler reply) {
      System.out.println("basicTestAndThen: " + reply);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                return 1;
              })
          .andThen(
              (value) -> {
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .onFailureRespond(
              (t) -> {
                System.out.println(t.getMessage());
                return "error " + t.getMessage();
              })
          .execute();
    }

    @Path("/basicTestSupplyWithErrorUnhandled")
    @GET
    public void basicTestSupplyWithErrorUnhandled(RestHandler reply) {
      System.out.println("basicTestSupplyWithErrorUnhandled: " + reply);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .execute();
    }

    @Path("/basicTestAndThenWithErrorUnhandled")
    @GET
    public void basicTestAndThenWithErrorUnhandled(RestHandler reply) {
      System.out.println("basicTestAndThen: " + reply);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                return 1;
              })
          .andThen(
              (value) -> {
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .execute();
    }

    @Path("/basicTestSupplyWithErrorSimpleRetry")
    @GET
    public void basicTestSupplyWithErrorSimpleRetry(RestHandler reply) {
      System.out.println("basicTestSupplyWithErrorSimpleRetry: " + reply);
      AtomicInteger counter = new AtomicInteger(0);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                counter.incrementAndGet();
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .onError(
              t -> {
                System.out.println(t.getMessage() + " counter:" + counter.get());
              })
          .retry(3)
          .onFailureRespond(
              (t) -> {
                return "error " + counter.get() + " " + t.getMessage();
              })
          .execute();
    }

    @Path("/basicTestAndThenWithErrorUnhandledRetry")
    @GET
    public void basicTestAndThenWithErrorUnhandledRetry(RestHandler reply) {
      System.out.println("basicTestAndThenWithErrorUnhandledRetry: " + reply);
      AtomicInteger counter = new AtomicInteger(1);
      reply
          .response()
          .blocking()
          .<Integer>supply(
              () -> {
                counter.decrementAndGet();
                return 1;
              })
          .andThen(
              (value) -> {
                counter.incrementAndGet();
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .onError(
              t -> {
                System.out.println(t.getMessage() + " counter:" + counter.get());
              })
          .retry(3)
          .onFailureRespond(
              (t) -> {
                return "error " + counter.get() + " " + t.getMessage();
              })
          .execute();
    }

    @Path("/basicTestSupplyWithErrorAndCircuitBreaker/:val")
    @GET
    public void basicTestSupplyWithErrorAndCircuitBreaker(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + val);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                if (val.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                return val;
              })
          .mapToStringResponse(
              (v) -> {
                return v;
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond((error) -> "failure")
          .execute();
    }

    @Path("/basicTestAndThenWithErrorAndCircuitBreaker/:val")
    @GET
    public void basicTestAndThenWithErrorAndCircuitBreaker(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + val);
      reply
          .response()
          .blocking()
          .supply(
              () -> {
                return val;
              })
          .andThen(
              (v) -> {
                if (v.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                return v;
              })
          .mapToStringResponse(
              (v) -> {
                return v;
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond((error) -> "failure")
          .execute();
    }

    @Path("/endpointOne")
    @GET
    public void rsEndpointOne(RestHandler reply) {
      System.out.println("wsEndpointOne: " + reply);
      reply
          .response()
          .<Integer>supply(
              (future) -> {
                future.complete(1);
              })
          .<String>andThen(
              (v, future) -> {
                future.complete(v + "test");
              })
          .mapToStringResponse(
              (val, future) -> {
                future.complete(val + " final");
              })
          .execute();
    }
  }
}
