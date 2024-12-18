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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientEventStringCircuitBreakerAsyncTest extends VertxTestBase {

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
  public void simpleSyncNoConnectionErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/simpleSyncNoConnectionErrorResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    assertEquals(body.toString(), "No handlers for address hello1");
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void simpleSyncNoConnectionErrorResponseStateful() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/simpleSyncNoConnectionErrorResponseStateful",
            resp ->
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());

                      assertEquals(body.toString(), "No handlers for address hello1");
                      HttpClientRequest request2 =
                          client.get(
                              "/wsService/simpleSyncNoConnectionErrorResponseStateful",
                              resp2 ->
                                  resp2.bodyHandler(
                                      body2 -> {
                                        System.out.println(
                                            "Got a createResponse: " + body2.toString());
                                        assertEquals(body2.toString(), "circuit open");
                                        // wait 1s, but circuit is still open
                                        vertx.setTimer(
                                            1205,
                                            handler -> {
                                              HttpClientRequest request3 =
                                                  client.get(
                                                      "/wsService/simpleSyncNoConnectionErrorResponseStateful",
                                                      resp3 ->
                                                          resp3.bodyHandler(
                                                              body3 -> {
                                                                System.out.println(
                                                                    "Got a createResponse: "
                                                                        + body3.toString());

                                                                assertEquals(
                                                                    body3.toString(),
                                                                    "circuit open");
                                                                // wait another 1s, now circuit
                                                                // should be closed
                                                                vertx.setTimer(
                                                                    2005,
                                                                    handler2 -> {
                                                                      HttpClientRequest request4 =
                                                                          client.get(
                                                                              "/wsService/simpleSyncNoConnectionErrorResponseStateful",
                                                                              resp4 ->
                                                                                  resp4.bodyHandler(
                                                                                      body4 -> {
                                                                                        System.out
                                                                                            .println(
                                                                                                "Got a createResponse: "
                                                                                                    + body4
                                                                                                        .toString());

                                                                                        assertEquals(
                                                                                            body4
                                                                                                .toString(),
                                                                                            "No handlers for address hello1");

                                                                                        // should be
                                                                                        // closed
                                                                                        testComplete();
                                                                                      }));
                                                                      request4.end();
                                                                    });
                                                              }));
                                              request3.end();
                                            });
                                      }));
                      request2.end();
                    }));
    request.end();

    await(80000, TimeUnit.MILLISECONDS);
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT2)
  public class WsServiceTwo extends VxmsEndpoint {

    @Path("/simpleSyncNoConnectionErrorResponse")
    @GET
    public void simpleSyncNoConnectionErrorResponse(RestHandler reply) {
      System.out.println("-------1");
      reply
          .eventBusRequest()
          .blocking()
          .send("hello1", "welt")
          .mapToStringResponse(
              (handler) -> {
                System.out.println("value from event  ");
                return handler.result().body().toString();
              })
          .onError(
              error -> {
                System.out.println(":::" + error.getMessage());
              })
          .retry(3)
          .onFailureRespond(t -> t.getMessage())
          .execute();
      System.out.println("-------2");
    }

    @Path("/simpleSyncNoConnectionErrorResponseStateful")
    @GET
    public void simpleSyncNoConnectionErrorResponseStateful(RestHandler reply) {
      System.out.println("-------1");
      reply
          .eventBusRequest()
          .blocking()
          .send("hello1", "welt")
          .mapToStringResponse(
              (handler) -> {
                System.out.println("value from event  ");
                return handler.result().body().toString();
              })
          .onError(
              error -> {
                System.out.println(":::" + error.getMessage());
              })
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond(t -> t.getMessage())
          .execute();
      System.out.println("-------2");
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
