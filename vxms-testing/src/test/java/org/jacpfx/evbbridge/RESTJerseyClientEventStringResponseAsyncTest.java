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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientEventStringResponseAsyncTest extends VertxTestBase {

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

    CountDownLatch latch2 = new CountDownLatch(2);
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

    client = getVertx().createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);
  }

  @Test
  public void simpleResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);
      client.request(HttpMethod.GET,"/wsService/simpleResponse").onComplete(response ->{

      });
    HttpClientRequest request =
        client.get(
            "/wsService/simpleResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse" + body.toString());

                    assertEquals(body.toString(), "hello");
                  });

              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void complexResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/complexResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse" + body.toString());

                    assertEquals(body.toString(), "hello");
                  });

              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void complexErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/complexErrorResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse" + body.toString());

                    assertEquals(body.toString(), "test exception");
                  });

              testComplete();
            });
    request.end();
    await();
  }

  @Test
  public void onErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/onFailurePass",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse" + body.toString());

                    assertEquals(body.toString(), "failed");
                  });

              testComplete();
            });
    request.end();
    await();
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT2)
  public class WsServiceTwo extends VxmsEndpoint {

    @Path("/simpleResponse")
    @GET
    public void simpleResponse(RestHandler reply) {
      reply.eventBusRequest().sendAndRespondRequest("hello", "welt");
    }

    @Path("/complexResponse")
    @GET
    public void complexResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello", "welt")
          .mapToStringResponse(handler -> handler.result().body().toString())
          .execute();
    }

    @Path("/complexErrorResponse")
    @GET
    public void complexErrorResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello", "welt")
          .mapToStringResponse(
              handler -> {
                throw new NullPointerException("test exception");
              })
          .onFailureRespond(error -> error.getMessage())
          .execute();
    }

    @Path("/complexChainErrorResponse")
    @GET
    public void complexChainErrorResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello", "welt")
          .mapToStringResponse(
              handler -> {
                throw new NullPointerException("test exception");
              })
          .retry(3)
          .onFailureRespond(error -> error.getMessage())
          .execute();
    }

    @Path("/onFailurePass")
    @GET
    public void onErrorResponse(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello1", "welt")
          .mapToStringResponse(handler -> handler.result().body().toString())
          .onFailureRespond(
              handler -> {
                System.err.println("::: " + handler.getMessage());
                return "failed";
              })
          .execute();
    }

    @Path("/onFailurePassError")
    @GET
    public void onFailurePassError(RestHandler reply) {
      reply
          .eventBusRequest()
          .blocking()
          .send("hello1", "welt")
          .mapToStringResponse(handler -> handler.result().body().toString())
          .onFailureRespond(
              handler -> {
                System.err.println("::: " + handler.getCause().getMessage());
                return "failed";
              })
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
}
