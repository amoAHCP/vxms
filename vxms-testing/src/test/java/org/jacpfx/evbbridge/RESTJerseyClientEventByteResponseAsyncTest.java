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

package org.jacpfx.evbbridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.entity.Payload;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.util.Serializer;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientEventByteResponseAsyncTest extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  public static final int PORT2 = 9999;
  public static final int PORT3 = 9991;
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

    CountDownLatch latch2 = new CountDownLatch(3);
    DeploymentOptions options = new DeploymentOptions().setInstances(1);
    options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
    // Deploy the module - the System property `vertx.modulename` will contain the name of the
    // module so you
    // don'failure have to hardecode it in your tests

    getVertx()
        .deployVerticle(
            new WsServiceTwo(),
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
    getVertx()
        .deployVerticle(
            new TestVerticle(),
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
    getVertx()
        .deployVerticle(
            new TestErrorVerticle(),
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
  public void complexByteResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/complexByteResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("hello").getValue());
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void complexByteErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/complexByteErrorResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("test exception").getValue());
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void simpleByteNoConnectionErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/simpleByteNoConnectionErrorResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("no connection").getValue());
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void simpleByteNoConnectionRetryErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/simpleByteNoConnectionRetryErrorResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("hello1").getValue());
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void simpleByteNoConnectionExceptionRetryErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/simpleByteNoConnectionExceptionRetryErrorResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("hello1").getValue());
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT2)
  public class WsServiceTwo extends VxmsEndpoint {

    @Path("/complexByteResponse")
    @GET
    public void complexByteResponse(RestHandler reply) {
      System.out.println("CALL");
      reply
          .eventBusRequest()
          .blocking()
          .send("hello", "welt")
          .mapToByteResponse(
              handler -> {
                Payload<String> p = new Payload<>(handler.result().body().toString());
                return Serializer.serialize(p);
              })
          .execute();
    }

    @Path("/complexByteErrorResponse")
    @GET
    public void complexByteErrorResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello", "welt")
          .mapToByteResponse(
              handler -> {
                throw new NullPointerException("test exception");
              })
          .onFailureRespond(
              error -> {
                try {
                  Payload<String> p = new Payload<>(error.getMessage());
                  return Serializer.serialize(p);
                } catch (IOException e) {
                  e.printStackTrace();
                }
                return new byte[0];
              })
          .execute();
    }

    @Path("/simpleByteNoConnectionErrorResponse")
    @GET
    public void simpleByteNoConnectionErrorResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello1", "welt")
          .mapToByteResponse(handler -> (byte[]) handler.result().body())
          .onFailureRespond(
              handler -> {
                try {
                  Payload<String> p = new Payload<>("no connection");
                  return Serializer.serialize(p);
                } catch (IOException e) {
                  e.printStackTrace();
                }
                return new byte[0];
              })
          .execute();
    }

    @Path("/simpleByteNoConnectionRetryErrorResponse")
    @GET
    public void simpleByteNoConnectionRetryErrorResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("error", "1")
          .mapToByteResponse(
              handler -> {
                Payload<String> p = new Payload<>(handler.result().body().toString() + "1");
                return Serializer.serialize(p);
              })
          .retry(3)
          .execute();
    }

    @Path("/simpleByteNoConnectionExceptionRetryErrorResponse")
    @GET
    public void simpleByteNoConnectionExceptionRetryErrorResponse(RestHandler reply) {
      AtomicInteger count = new AtomicInteger(0);
      reply
          .eventBusRequest()
          .blocking()
          .send("hello", "welt")
          .mapToByteResponse(
              handler -> {
                System.out.println("retry: " + count.get());
                if (count.incrementAndGet() < 3) {
                  throw new NullPointerException("test");
                }
                Payload<String> p = new Payload<>(handler.result().body().toString() + "1");
                return Serializer.serialize(p);
              })
          .retry(3)
          .execute();
    }
  }

  public class TestVerticle extends AbstractVerticle {

    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      System.out.println("start");
      vertx
          .eventBus()
          .consumer(
              "hello",
              handler -> {
                System.out.println("request::" + handler.body().toString());
                handler.reply("hello");
              });
      startFuture.complete();
    }
  }

  public class TestErrorVerticle extends AbstractVerticle {

    private AtomicLong counter = new AtomicLong(0L);

    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      System.out.println("start");
      vertx
          .eventBus()
          .consumer(
              "error",
              handler -> {
                System.out.println("request::" + handler.body().toString());
                if (counter.incrementAndGet() % 3 == 0) {
                  System.out.println("reply::" + handler.body().toString());
                  handler.reply("hello");
                } else {
                  System.out.println("fail::" + handler.body().toString());
                  handler.fail(500, "my error");
                }
              });
      startFuture.complete();
    }
  }
}
