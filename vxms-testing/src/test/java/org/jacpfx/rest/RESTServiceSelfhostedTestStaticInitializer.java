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

import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
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
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.entity.encoder.ExampleStringEncoder;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.util.Serializer;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTServiceSelfhostedTestStaticInitializer extends VertxTestBase {

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
  public void endpointOne() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointOne",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println("Got a createResponse: " + body.toString());
                  assertEquals(body.toString(), "test");
                  testComplete();
                }));
    request.end();
    await();
  }

  @Test
  public void endpointTwo() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointTwo/123",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println("Got a createResponse: " + body.toString());
                  assertEquals(body.toString(), "123");
                  testComplete();
                }));
    request.end();
    await();
  }

  @Test
  public void endpointThree() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointThree?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    assertEquals(body.toString(), "123456");
                  });
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointFourErrorRetryTest() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointFourErrorRetryTest?val=123&tmp=456",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println("Got a createResponse: " + body.toString());
                  assertEquals(body.toString(), "123456");
                  testComplete();
                }));
    request.end();
    await();
  }

  @Test
  public void endpointFourErrorReturnRetryTest() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointFourErrorReturnRetryTest?productType=123&product=456",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println(
                      "Got a createResponse endpointFourErrorReturnRetryTest: "
                          + body.toString());
                  assertEquals(body.toString(), "456123");
                  testComplete();
                }));
    request.end();
    await();
  }

  @Test
  public void endpointFive() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointFive?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Payload<String> pp = new Gson().fromJson(body.toString(), Payload.class);
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());
                  });
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointFive_error() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointFive_error?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Payload<String> pp = new Gson().fromJson(body.toString(), Payload.class);
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());
                  });
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointSix() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointSix?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());
                  });
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointSeven() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointSeven?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());
                  });
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointSeven_error() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointSeven_error?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());
                  });
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointEight_header() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointEight_header?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println(
                        "Got a createResponse endpointFourErrorReturnRetryTest: "
                            + body.toString());

                    assertEquals(body.toString(), "123456");
                  });
              String contentType = resp.getHeader("Content-Type");
              assertEquals(contentType, "application/json");
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointEight_put_header() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointEight_put_header?val=123&tmp=456",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println(
                        "Got a createResponse endpointFourErrorReturnRetryTest: "
                            + body.toString());

                    assertEquals(body.toString(), "123456");
                  });
              String contentType = resp.getHeader("Content-Type");
              assertEquals(contentType, "application/json");
              String key = resp.getHeader("key");
              assertEquals(key, "val");
              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void endpointNine_exception() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/endpointNine_exception?val=123&tmp=456",
            resp -> {
              assertEquals(500, resp.statusCode());
              assertEquals("test", resp.statusMessage());
              testComplete();
            });
    request.end();
    await();
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends AbstractVerticle {

    @Override
    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      VxmsEndpoint.start(startFuture, this);
    }

    @Path("/endpointOne")
    @GET
    public void rsEndpointOne(RestHandler reply) {
      System.out.println("wsEndpointOne: " + reply);
      reply.response().stringResponse((future) -> future.complete("test")).execute();
    }

    @Path("/endpointTwo/:help")
    @GET
    public void rsEndpointTwo(RestHandler handler) {
      String productType = handler.request().param("help");
      System.out.println("wsEndpointTwo: " + handler);
      handler.response().stringResponse((future) -> future.complete(productType)).execute();
    }

    @Path("/endpointThree")
    @GET
    public void rsEndpointThree(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      handler
          .response()
          .stringResponse((future) -> future.complete(productType + product))
          .execute();
    }

    @Path("/endpointFourErrorRetryTest")
    @GET
    public void rsEndpointFourErrorRetryTest(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      AtomicInteger count = new AtomicInteger(4);
      handler
          .response()
          .stringResponse(
              (future) -> {
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw:" + count.get());
                  throw new NullPointerException("test");
                }
                future.complete(productType + product);
              })
          .onError(
              error -> {
                error.printStackTrace();
                System.out.println("count: " + count.get());
              })
          .retry(3)
          .onFailureRespond((e, response) -> response.complete(productType + product))
          .execute();
    }

    @Path("/endpointFourErrorReturnRetryTest")
    @GET
    public void rsEndpointFourErrorReturnRetryTest(RestHandler handler) {
      String productType = handler.request().param("productType");
      String product = handler.request().param("product");
      System.out.println("wsEndpointTwo: " + handler);
      AtomicInteger count = new AtomicInteger(4);
      handler
          .response()
          .stringResponse(
              (future) -> {
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw:" + count.get());
                  throw new NullPointerException("test");
                }
                future.complete(productType + product);
              })
          .onError(
              error -> System.out.println("retry: " + count.get() + "   " + error.getStackTrace()))
          .retry(3)
          .closeCircuitBreaker(0l)
          .onFailureRespond((error, response) -> response.complete(product + productType))
          .execute();
    }

    @Path("/endpointFive")
    @GET
    public void rsEndpointFive(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      Payload<String> pp = new Payload<>(productType + product);
      handler
          .response()
          .objectResponse((future) -> future.complete(pp), new ExampleStringEncoder())
          .execute();
    }

    @Path("/endpointFive_error")
    @GET
    public void rsEndpointFive_error(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      Payload<String> pp = new Payload<>(productType + product);
      AtomicInteger count = new AtomicInteger(4);
      handler
          .response()
          .objectResponse(
              (future) -> {
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw:" + count.get());
                  throw new NullPointerException("test");
                }
                future.complete(new Payload<>("hallo"));
              },
              new ExampleStringEncoder())
          .retry(3)
          .onFailureRespond((error, future) -> future.complete(pp), new ExampleStringEncoder())
          .execute();
    }

    @Path("/endpointSix")
    @GET
    public void rsEndpointSix(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      Payload<String> pp = new Payload<>(productType + product);
      handler
          .response()
          .objectResponse((future) -> future.complete(pp), new ExampleByteEncoder())
          .execute();
    }

    @Path("/endpointSeven")
    @GET
    public void rsEndpointSeven(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      Payload<String> pp = new Payload<>(productType + product);
      handler
          .response()
          .byteResponse((future) -> future.complete(Serializer.serialize(pp)))
          .execute();
    }

    @Path("/endpointSeven_error")
    @GET
    public void rsEndpointSeven_error(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      AtomicInteger count = new AtomicInteger(4);
      Payload<String> pp = new Payload<>(productType + product);
      handler
          .response()
          .byteResponse(
              (future) -> {
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw:" + count.get());
                  throw new NullPointerException("test");
                }
                future.complete(Serializer.serialize(pp));
              })
          .retry(3)
          .onFailureRespond(
              (error, future) -> {
                try {
                  future.complete(Serializer.serialize(pp));
                } catch (IOException e) {
                  e.printStackTrace();
                }
                future.complete(null);
              })
          .execute();
    }

    @Path("/endpointEight_header")
    @GET
    public void rsEndpointEight_header(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      AtomicInteger count = new AtomicInteger(4);
      Payload<String> pp = new Payload<>(productType + product);
      handler
          .response()
          .stringResponse(
              (future) -> {
                future.complete(productType + product);
              })
          .execute("application/json");
    }

    @Path("/endpointEight_put_header")
    @GET
    public void rsEndpointEight_put_header(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      AtomicInteger count = new AtomicInteger(4);
      Payload<String> pp = new Payload<>(productType + product);
      handler
          .response()
          .stringResponse(
              (future) -> {
                future.complete(productType + product);
              })
          .putHeader("key", "val")
          .execute("application/json");
    }

    @Path("/endpointNine_exception")
    @GET
    public void rsEndpointNine_exception(RestHandler handler) {
      String productType = handler.request().param("val");
      String product = handler.request().param("tmp");
      System.out.println("wsEndpointTwo: " + handler);
      handler
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("test");
              })
          .putHeader("key", "val")
          .execute("application/json");
    }
  }
}
