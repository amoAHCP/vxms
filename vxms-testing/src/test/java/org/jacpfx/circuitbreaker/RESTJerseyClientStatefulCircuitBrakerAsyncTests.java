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

package org.jacpfx.circuitbreaker;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientStatefulCircuitBrakerAsyncTests extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private static final int MAX_RESPONSE_ELEMENTS = 4;
  private static final String HOST = "127.0.0.1";
  private HttpClient client;

  protected int getNumNodes() {
    return 1;
  }

  @Before
  public void startVerticles() throws InterruptedException {

    CountDownLatch latch2 = new CountDownLatch(1);
    DeploymentOptions options = new DeploymentOptions().setInstances(1);
    options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
    // Deploy the module - the System property `vertx.modulename` will contain the name of the
    // module so you
    // don'failure have to hardecode it in your tests

    vertx.deployVerticle(
        new WsServiceOne(),
        options,
        asyncResult -> {
          // Deployment is asynchronous and this this handler will be called when it's complete (or
          // failed)
          System.out.println("start service: " + asyncResult.succeeded());
          assertTrue(asyncResult.succeeded());
          assertNotNull("deploymentID should not be null", asyncResult.result());
          // If deployed correctly then start the tests!
          //   latch2.countDown();

          latch2.countDown();
        });

    client = vertx.createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);
  }

  @Test
  public void stringGETResponseCircuitBaseTest() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseCircuitBaseTest/crash",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println("Got a createResponse: " + body.toString());

                  assertEquals(body.toString(), "failure");
                  HttpClientRequest request2 =
                      client.get(
                          "/wsService/stringGETResponseCircuitBaseTest/value",
                          resp2 -> resp2.bodyHandler(
                              body2 -> {
                                System.out.println("Got a createResponse: " + body2.toString());
                                Assert.assertEquals(body2.toString(), "failure");
                                // wait 1s, but circuit is still open
                                vertx.setTimer(
                                    1205,
                                    handler -> {
                                      HttpClientRequest request3 =
                                          client.get(
                                              "/wsService/stringGETResponseCircuitBaseTest/value",
                                              resp3 -> resp3.bodyHandler(
                                                  body3 -> {
                                                    System.out.println(
                                                        "Got a createResponse: " + body3
                                                            .toString());

                                                    Assert.assertEquals(body3.toString(),
                                                        "failure");
                                                    // wait another 1s, now circuit
                                                    // should be closed
                                                    vertx.setTimer(
                                                        2005,
                                                        handler2 -> {
                                                          HttpClientRequest request4 =
                                                              client.get(
                                                                  "/wsService/stringGETResponseCircuitBaseTest/value",
                                                                  resp4 -> resp4.bodyHandler(
                                                                      body4 -> {
                                                                        System.out.println(
                                                                            "Got a createResponse: "
                                                                                + body4.toString());

                                                                        Assert.assertEquals(
                                                                            body4.toString(),
                                                                            "value");

                                                                        // should be closed
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

  @Test
  public void stringGETResponseCircuitBaseWithDelayTest() throws InterruptedException {


    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseCircuitBaseWithDelayTest/crash",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println("Got a createResponse: " + body.toString());

                  assertEquals(body.toString(), "failure");
                  HttpClientRequest request2 =
                      client.get(
                          "/wsService/stringGETResponseCircuitBaseWithDelayTest/value",
                          resp2 -> resp2.bodyHandler(
                              body2 -> {
                                System.out.println("Got a createResponse: " + body2.toString());
                                Assert.assertEquals(body2.toString(), "failure");
                                // wait 1s, but circuit is still open
                                vertx.setTimer(
                                    1205,
                                    handler -> {
                                      HttpClientRequest request3 =
                                          client.get(
                                              "/wsService/stringGETResponseCircuitBaseWithDelayTest/value",
                                              resp3 -> resp3.bodyHandler(
                                                  body3 -> {
                                                    System.out.println(
                                                        "Got a createResponse: " + body3
                                                            .toString());

                                                    Assert.assertEquals(body3.toString(),
                                                        "failure");
                                                    // wait another 1s, now circuit
                                                    // should be closed
                                                    vertx.setTimer(
                                                        2005,
                                                        handler2 -> {
                                                          HttpClientRequest request4 =
                                                              client.get(
                                                                  "/wsService/stringGETResponseCircuitBaseWithDelayTest/value",
                                                                  resp4 -> resp4.bodyHandler(
                                                                      body4 -> {
                                                                        System.out.println(
                                                                            "Got a createResponse: "
                                                                                + body4.toString());

                                                                        Assert.assertEquals(
                                                                            body4.toString(),
                                                                            "value");

                                                                        // should be closed
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

  @Test
  public void stringGETResponseCircuitBaseWithTimeoutTest() throws InterruptedException {


    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseCircuitBaseWithTimeoutTest/crash",
            resp -> resp.bodyHandler(
                body -> {
                  System.out.println("Got a createResponse: " + body.toString());

                  assertEquals(body.toString(), "failure");
                  HttpClientRequest request2 =
                      client.get(
                          "/wsService/stringGETResponseCircuitBaseWithTimeoutTest/value",
                          resp2 -> resp2.bodyHandler(
                              body2 -> {
                                System.out.println("Got a createResponse: " + body2.toString());
                                Assert.assertEquals(body2.toString(), "failure");
                                // wait 1s, but circuit is still open
                                vertx.setTimer(
                                    1205,
                                    handler -> {
                                      HttpClientRequest request3 =
                                          client.get(
                                              "/wsService/stringGETResponseCircuitBaseWithTimeoutTest/value",
                                              resp3 -> resp3.bodyHandler(
                                                  body3 -> {
                                                    System.out.println(
                                                        "Got a createResponse: " + body3
                                                            .toString());

                                                    Assert.assertEquals(body3.toString(),
                                                        "failure");
                                                    // wait another 1s, now circuit
                                                    // should be closed
                                                    vertx.setTimer(
                                                        2005,
                                                        handler2 -> {
                                                          HttpClientRequest request4 =
                                                              client.get(
                                                                  "/wsService/stringGETResponseCircuitBaseWithTimeoutTest/value",
                                                                  resp4 -> resp4.bodyHandler(
                                                                      body4 -> {
                                                                        System.out.println(
                                                                            "Got a createResponse: "
                                                                                + body4.toString());

                                                                        Assert.assertEquals(
                                                                            body4.toString(),
                                                                            "value");

                                                                        // should be closed
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

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    ///// ------------- sync blocking ----------------

    @Path("/stringGETResponseCircuitBaseTest/:val")
    @GET
    public void stringGETResponseCircuitBaseTest(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                if (val.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                return val;
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond(error -> "failure")
          .execute();
    }

    @Path("/stringGETResponseCircuitBaseWithDelayTest/:val")
    @GET
    public void stringGETResponseCircuitBaseWithDelayTest(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                if (val.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                return val;
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .delay(2000)
          .onFailureRespond(error -> "failure")
          .execute();
    }

    @Path("/stringGETResponseCircuitBaseWithTimeoutTest/:val")
    @GET
    public void stringGETResponseCircuitBaseWithTimeoutTest(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                if (val.equals("crash")) {
                  System.out.println("start sleep");
                  Thread.sleep(8000);
                  System.out.println("stop sleep");
                }
                return val;
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .timeout(500)
          .onFailureRespond(error -> "failure")
          .execute();
    }
  }
}
