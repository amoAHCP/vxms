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

import com.google.gson.Gson;
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
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleStringEncoder;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.util.Serializer;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientStatefulCircuitBrakerTests extends VertxTestBase {

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
    options.setConfig(
        new JsonObject()
            .put("clustered", false)
            .put("host", HOST)
            .put("circuit-breaker-scope", "local"));
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
  public void stringGETResponseCircuitBaseTest() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseCircuitBaseTest/crash",
            resp ->
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());

                      assertEquals(body.toString(), "failure");
                      HttpClientRequest request2 =
                          client.get(
                              "/wsService/stringGETResponseCircuitBaseTest/value",
                              resp2 ->
                                  resp2.bodyHandler(
                                      body2 -> {
                                        System.out.println(
                                            "Got a createResponse: " + body2.toString());
                                        assertEquals(body2.toString(), "failure");
                                        // wait 1s, but circuit is still open
                                        vertx.setTimer(
                                            1205,
                                            handler -> {
                                              HttpClientRequest request3 =
                                                  client.get(
                                                      "/wsService/stringGETResponseCircuitBaseTest/value",
                                                      resp3 ->
                                                          resp3.bodyHandler(
                                                              body3 -> {
                                                                System.out.println(
                                                                    "Got a createResponse: "
                                                                        + body3.toString());

                                                                assertEquals(
                                                                    body3.toString(), "failure");
                                                                // wait another 1s, now circuit
                                                                // should be closed
                                                                vertx.setTimer(
                                                                    2005,
                                                                    handler2 -> {
                                                                      HttpClientRequest request4 =
                                                                          client.get(
                                                                              "/wsService/stringGETResponseCircuitBaseTest/value",
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
                                                                                            "value");

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

  @Test
  public void objectGETResponseCircuitBaseTest() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/objectGETResponseCircuitBaseTest/crash",
            resp ->
                resp.bodyHandler(
                    body -> {
                      System.out.println("Got a createResponse: " + body.toString());

                      Payload<String> pp = new Gson().fromJson(body.toString(), Payload.class);
                      assertEquals(new Payload<String>("failure").getValue(), pp.getValue());
                      HttpClientRequest request2 =
                          client.get(
                              "/wsService/objectGETResponseCircuitBaseTest/value",
                              resp2 ->
                                  resp2.bodyHandler(
                                      body2 -> {
                                        System.out.println(
                                            "Got a createResponse: " + body2.toString());
                                        Payload<String> pp2 =
                                            new Gson().fromJson(body2.toString(), Payload.class);
                                        assertEquals(
                                            new Payload<String>("failure").getValue(),
                                            pp2.getValue());
                                        // wait 1s, but circuit is still open
                                        vertx.setTimer(
                                            1205,
                                            handler -> {
                                              HttpClientRequest request3 =
                                                  client.get(
                                                      "/wsService/objectGETResponseCircuitBaseTest/value",
                                                      resp3 ->
                                                          resp3.bodyHandler(
                                                              body3 -> {
                                                                System.out.println(
                                                                    "Got a createResponse: "
                                                                        + body3.toString());

                                                                Payload<String> pp3 =
                                                                    new Gson()
                                                                        .fromJson(
                                                                            body3.toString(),
                                                                            Payload.class);
                                                                assertEquals(
                                                                    new Payload<String>("failure")
                                                                        .getValue(),
                                                                    pp3.getValue());
                                                                // wait another 1s, now circuit
                                                                // should be closed
                                                                vertx.setTimer(
                                                                    2005,
                                                                    handler2 -> {
                                                                      HttpClientRequest request4 =
                                                                          client.get(
                                                                              "/wsService/objectGETResponseCircuitBaseTest/value",
                                                                              resp4 ->
                                                                                  resp4.bodyHandler(
                                                                                      body4 -> {
                                                                                        System.out
                                                                                            .println(
                                                                                                "Got a createResponse: "
                                                                                                    + body4
                                                                                                        .toString());
                                                                                        Payload<
                                                                                                String>
                                                                                            pp4 =
                                                                                                new Gson()
                                                                                                    .fromJson(
                                                                                                        body4
                                                                                                            .toString(),
                                                                                                        Payload
                                                                                                            .class);
                                                                                        assertEquals(
                                                                                            new Payload<
                                                                                                    String>(
                                                                                                    "value")
                                                                                                .getValue(),
                                                                                            pp4
                                                                                                .getValue());

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

  @Test
  public void byteGETResponseCircuitBaseTest() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/byteGETResponseCircuitBaseTest/crash",
            resp ->
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
                      assertEquals(new Payload<String>("failure").getValue(), pp.getValue());
                      HttpClientRequest request2 =
                          client.get(
                              "/wsService/byteGETResponseCircuitBaseTest/value",
                              resp2 ->
                                  resp2.bodyHandler(
                                      body2 -> {
                                        System.out.println(
                                            "Got a createResponse: " + body2.toString());
                                        Payload<String> pp2 = null;
                                        try {
                                          pp2 =
                                              (Payload<String>)
                                                  Serializer.deserialize(body2.getBytes());
                                        } catch (IOException e) {
                                          e.printStackTrace();
                                        } catch (ClassNotFoundException e) {
                                          e.printStackTrace();
                                        }
                                        assertEquals(
                                            new Payload<String>("failure").getValue(),
                                            pp2.getValue());
                                        // wait 1s, but circuit is still open
                                        vertx.setTimer(
                                            1205,
                                            handler -> {
                                              HttpClientRequest request3 =
                                                  client.get(
                                                      "/wsService/byteGETResponseCircuitBaseTest/value",
                                                      resp3 ->
                                                          resp3.bodyHandler(
                                                              body3 -> {
                                                                System.out.println(
                                                                    "Got a createResponse: "
                                                                        + body3.toString());

                                                                Payload<String> pp3 = null;
                                                                try {
                                                                  pp3 =
                                                                      (Payload<String>)
                                                                          Serializer.deserialize(
                                                                              body.getBytes());
                                                                } catch (IOException e) {
                                                                  e.printStackTrace();
                                                                } catch (ClassNotFoundException e) {
                                                                  e.printStackTrace();
                                                                }
                                                                assertEquals(
                                                                    new Payload<String>("failure")
                                                                        .getValue(),
                                                                    pp3.getValue());
                                                                // wait another 1s, now circuit
                                                                // should be closed
                                                                vertx.setTimer(
                                                                    2005,
                                                                    handler2 -> {
                                                                      HttpClientRequest request4 =
                                                                          client.get(
                                                                              "/wsService/byteGETResponseCircuitBaseTest/value",
                                                                              resp4 ->
                                                                                  resp4.bodyHandler(
                                                                                      body4 -> {
                                                                                        System.out
                                                                                            .println(
                                                                                                "Got a createResponse: "
                                                                                                    + body4
                                                                                                        .toString());
                                                                                        Payload<
                                                                                                String>
                                                                                            pp4 =
                                                                                                null;
                                                                                        try {
                                                                                          pp4 =
                                                                                              (Payload<
                                                                                                      String>)
                                                                                                  Serializer
                                                                                                      .deserialize(
                                                                                                          body4
                                                                                                              .getBytes());
                                                                                        } catch (
                                                                                            IOException
                                                                                                e) {
                                                                                          e
                                                                                              .printStackTrace();
                                                                                        } catch (
                                                                                            ClassNotFoundException
                                                                                                e) {
                                                                                          e
                                                                                              .printStackTrace();
                                                                                        }
                                                                                        assertEquals(
                                                                                            new Payload<
                                                                                                    String>(
                                                                                                    "value")
                                                                                                .getValue(),
                                                                                            pp4
                                                                                                .getValue());

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

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    ///// ------------- sync blocking ----------------

    @Path("/stringGETResponseCircuitBaseTest/:val")
    @GET
    public void stringGETResponseCircuitBaseTest(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("stringResponse: " + val);
      reply
          .response()
          .stringResponse(
              (future) -> {
                if (val.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                future.complete(val);
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond((error, future) -> future.complete("failure"))
          .execute();
    }

    @Path("/objectGETResponseCircuitBaseTest/:val")
    @GET
    public void objectGETResponseCircuitBaseTest(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("object: " + val);
      reply
          .response()
          .objectResponse(
              (future) -> {
                if (val.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                future.complete(new Payload<>(val));
              },
              new ExampleStringEncoder())
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond(
              (error, future) -> future.complete(new Payload<>("failure")),
              new ExampleStringEncoder())
          .execute();
    }

    @Path("/byteGETResponseCircuitBaseTest/:val")
    @GET
    public void byteGETResponseCircuitBaseTest(RestHandler reply) {
      final String val = reply.request().param("val");
      System.out.println("byte: " + val);
      reply
          .response()
          .byteResponse(
              (future) -> {
                if (val.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                future.complete(Serializer.serialize(new Payload<>(val)));
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond(
              (error, future) -> future.complete(Serializer.serialize(new Payload<>("failure"))))
          .execute();
    }
  }
}
