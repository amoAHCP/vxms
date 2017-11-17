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
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.event.annotation.Consume;
import org.jacpfx.vxms.event.response.EventbusHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class EventbusBlockingChainingStringTest extends VertxTestBase {

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
    WsServiceOne one = new WsServiceOne();
    one.init(vertx, vertx.getOrCreateContext());
    getVertx()
        .deployVerticle(
            one,
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
  public void simpleBlockingStringResponse() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestSupply",
            "hello",
            res -> {
              assertTrue(res.succeeded());
              Assert.assertEquals(res.result().body().toString(), "1 final");
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestAndThen() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestAndThen",
            "hello",
            res -> {
              assertTrue(res.succeeded());
              Assert.assertEquals(res.result().body().toString(), "2 final");
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestSupplyWithError() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestSupplyWithError",
            "hello",
            res -> {
              System.out.println("Got a createResponse: " + res.result().body().toString());
              Assert.assertEquals(res.result().body().toString(), "error test error");
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestAndThenWithError() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestAndThenWithError",
            "hello",
            res -> {
              System.out.println("Got a createResponse: " + res.result().body().toString());
              Assert.assertEquals(res.result().body().toString(), "error test error");
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestSupplyWithErrorUnhandled() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestSupplyWithErrorUnhandled",
            "hello",
            res -> {
              Assert.assertTrue(res.failed());
              Assert.assertTrue(res.cause().getMessage().equalsIgnoreCase("test error"));
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestSupplyWithErrorSimpleRetry() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestSupplyWithErrorSimpleRetry",
            "hello",
            res -> {
              System.out.println("Got a createResponse: " + res.result().body().toString());
              Assert.assertEquals(res.result().body().toString(), "error 4 test error");
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestAndThenWithErrorUnhandledRetry() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestAndThenWithErrorUnhandledRetry",
            "hello",
            res -> {
              System.out.println("Got a createResponse: " + res.result().body().toString());
              Assert.assertEquals(res.result().body().toString(), "error 4 test error");
              testComplete();
            });
    await();
  }

  @Test
  public void basicTestSupplyWithErrorAndCircuitBreaker() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestSupplyWithErrorAndCircuitBreaker",
            "crash",
            res -> {
              assertTrue(res.succeeded());
              assertEquals("failure", res.result().body().toString());
              System.out.println("out: " + res.result().body().toString());
              getVertx()
                  .eventBus()
                  .send(
                      SERVICE_REST_GET + "/basicTestSupplyWithErrorAndCircuitBreaker",
                      "val",
                      res2 -> {
                        assertTrue(res2.succeeded());
                        assertEquals("failure", res2.result().body().toString());
                        System.out.println("out: " + res2.result().body().toString());

                        // wait 1s, but circuit is still open
                        vertx.setTimer(
                            1205,
                            handler -> {
                              getVertx()
                                  .eventBus()
                                  .send(
                                      SERVICE_REST_GET
                                          + "/basicTestSupplyWithErrorAndCircuitBreaker",
                                      "val",
                                      res3 -> {
                                        assertTrue(res3.succeeded());
                                        assertEquals("failure", res3.result().body().toString());
                                        System.out.println(
                                            "out: " + res3.result().body().toString());

                                        // wait another 1s, now circuit should be closed
                                        vertx.setTimer(
                                            1005,
                                            handler2 -> {
                                              getVertx()
                                                  .eventBus()
                                                  .send(
                                                      SERVICE_REST_GET
                                                          + "/basicTestSupplyWithErrorAndCircuitBreaker",
                                                      "val",
                                                      res4 -> {
                                                        assertTrue(res4.succeeded());
                                                        assertEquals(
                                                            "val", res4.result().body().toString());
                                                        System.out.println(
                                                            "out: "
                                                                + res4.result().body().toString());

                                                        testComplete();
                                                      });
                                            });
                                      });
                            });
                      });
            });
    await();
  }

  @Test
  public void basicTestAndThenWithErrorAndCircuitBreaker() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/basicTestAndThenWithErrorAndCircuitBreaker",
            "crash",
            res -> {
              assertTrue(res.succeeded());
              assertEquals("failure", res.result().body().toString());
              System.out.println("out: " + res.result().body().toString());
              getVertx()
                  .eventBus()
                  .send(
                      SERVICE_REST_GET + "/basicTestAndThenWithErrorAndCircuitBreaker",
                      "val",
                      res2 -> {
                        assertTrue(res2.succeeded());
                        assertEquals("failure", res2.result().body().toString());
                        System.out.println("out: " + res2.result().body().toString());

                        // wait 1s, but circuit is still open
                        vertx.setTimer(
                            1205,
                            handler -> {
                              getVertx()
                                  .eventBus()
                                  .send(
                                      SERVICE_REST_GET
                                          + "/basicTestAndThenWithErrorAndCircuitBreaker",
                                      "val",
                                      res3 -> {
                                        assertTrue(res3.succeeded());
                                        assertEquals("failure", res3.result().body().toString());
                                        System.out.println(
                                            "out: " + res3.result().body().toString());

                                        // wait another 1s, now circuit should be closed
                                        vertx.setTimer(
                                            1005,
                                            handler2 -> {
                                              getVertx()
                                                  .eventBus()
                                                  .send(
                                                      SERVICE_REST_GET
                                                          + "/basicTestAndThenWithErrorAndCircuitBreaker",
                                                      "val",
                                                      res4 -> {
                                                        assertTrue(res4.succeeded());
                                                        assertEquals(
                                                            "val", res4.result().body().toString());
                                                        System.out.println(
                                                            "out: "
                                                                + res4.result().body().toString());

                                                        testComplete();
                                                      });
                                            });
                                      });
                            });
                      });
            });
    await();
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Consume("/basicTestSupply")
    public void simpleStringResponse(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .<Integer>supply(() -> 1)
          .mapToStringResponse((val) -> val + " final")
          .execute();
    }

    @Consume("/basicTestAndThen")
    public void basicTestAndThen(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .<Integer>supply(
              () -> 1)
          .<String>andThen(
              (value) -> {
                return value + 1 + "";
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .execute();
    }

    @Consume("/basicTestSupplyWithError")
    public void basicTestSupplyWithError(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .<Integer>supply(
              () -> {
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

    @Consume("/basicTestAndThenWithError")
    public void basicTestAndThenWithError(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .<Integer>supply(
              ()
               -> 1)
          .<String>andThen(
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

    @Consume("/basicTestSupplyWithErrorUnhandled")
    public void basicTestSupplyWithErrorUnhandled(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .<Integer>supply(
              () -> {
                throw new NullPointerException("test error");
              })
          .mapToStringResponse(
              (val) -> {
                return val + " final";
              })
          .execute();
    }

    @Consume("/basicTestSupplyWithErrorSimpleRetry")
    public void basicTestSupplyWithErrorSimpleRetry(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      AtomicInteger counter = new AtomicInteger(0);
      reply
          .response()
          .blocking()
          .<Integer>supply(
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

    @Consume("/basicTestAndThenWithErrorUnhandledRetry")
    public void basicTestAndThenWithErrorUnhandledRetry(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      AtomicInteger counter = new AtomicInteger(1);
      reply
          .response()
          .blocking()
          .<Integer>supply(
              () -> {
                counter.decrementAndGet();
                return 1;
              })
          .<String>andThen(
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

    @Consume("/basicTestSupplyWithErrorAndCircuitBreaker")
    public void basicTestSupplyWithErrorAndCircuitBreaker(EventbusHandler reply) {
      String val = reply.request().body().toString();
      System.out.println("stringResponse: " + val);
      reply
          .response()
          .blocking()
          .<String>supply(
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

    @Consume("/basicTestAndThenWithErrorAndCircuitBreaker")
    public void basicTestAndThenWithErrorAndCircuitBreaker(EventbusHandler reply) {
      String val = reply.request().body().toString();
      System.out.println("stringResponse: " + val);
      reply
          .response()
          .blocking()
          .<String>supply(
              () -> {
                return val;
              })
          .<String>andThen(
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
  }
}
