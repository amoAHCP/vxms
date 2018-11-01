/*
 * Copyright [2018] [Andy Moncsek]
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleStringEncoder;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.util.Serializer;
import org.jacpfx.vxms.rest.RouteBuilder;
import org.jacpfx.vxms.rest.VxmsRESTRoutes;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTVerticleRouteBuilderSelfhostedAsyncTest extends VertxTestBase {

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
  public void asyncStringResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/asyncStringResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    assertEquals(body.toString(), "test");
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void asyncStringResponseParameter() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/asyncStringResponseParameter/123",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    assertEquals(body.toString(), "123");
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void asyncByteResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/asyncByteResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                      e.printStackTrace(); fail();
                    } catch (ClassNotFoundException e) {
                      e.printStackTrace(); fail();

                    }
                    assertEquals(pp.getValue(), new Payload<>("test").getValue());
                    testComplete();
                  });

            });
    request.end();
    await();
  }
  @Test
  public void asyncByteResponseParameter() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/asyncByteResponseParameter/123",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    Payload<String> pp = null;
                    try {
                      pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {

                      e.printStackTrace();
                     fail();
                    } catch (ClassNotFoundException e) {

                      e.printStackTrace();
                      fail();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123").getValue());
                    testComplete();
                  });

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
      VxmsRESTRoutes routes =
          VxmsRESTRoutes.init()
              .route(RouteBuilder.get("/asyncStringResponse", this::rsAsyncStringResponse))
              .route(RouteBuilder.get("/asyncByteResponse", this::rsAsyncByteResponse))
              .route(RouteBuilder.get("/asyncObjectResponse", this::rsAsyncObjectResponse))
              .route(RouteBuilder.get("/asyncStringResponseParameter/:help", this::rsAsyncStringResponseParameter))
              .route(RouteBuilder.get("/asyncByteResponseParameter/:help", this::rsAsyncByteResponseParameter));
      VxmsEndpoint.init(startFuture, this, routes);
    }

    public void rsAsyncStringResponse(RestHandler reply) {
      System.out.println("asyncStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return "test";
              })
          .execute();
    }

    public void rsAsyncStringResponseParameter(RestHandler handler) {
      String productType = handler.request().param("help");
      System.out.println("asyncStringResponseParameter: " + handler);
      handler
          .response()
          .blocking()
          .stringResponse(
              () -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return productType;
              })
          .execute();
    }

    @Path("/asyncByteResponse")
    @GET
    public void rsAsyncByteResponse(RestHandler reply) {
      System.out.println("asyncStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .byteResponse(
              () -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return Serializer.serialize(new Payload<String>("test"));
              })
          .execute();
    }

    public void rsAsyncByteResponseParameter(RestHandler handler) {
      String productType = handler.request().param("help");
      System.out.println("asyncStringResponseParameter: " + handler);
      handler
          .response()
          .blocking()
          .byteResponse(
              () -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return Serializer.serialize(new Payload<String>(productType));
              })
          .execute();
    }

    @Path("/asyncObjectResponse")
    @GET
    public void rsAsyncObjectResponse(RestHandler reply)  {
      System.out.println("asyncStringResponse: " + reply);
      reply
          .response()
          .blocking()
          .objectResponse(
              () -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return new Payload<String>("test");
              },
              new ExampleStringEncoder())
          .execute();
    }


  }
}
