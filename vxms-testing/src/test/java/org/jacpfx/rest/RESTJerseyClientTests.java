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
import java.util.concurrent.ExecutionException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientTests extends VertxTestBase {

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
  public void stringGETResponse() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponse",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    assertEquals(body.toString(), "test");
                    testComplete();
                  });

            }).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.end();
    await();

  }

  @Test
  public void stringPOST() throws InterruptedException, ExecutionException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.post(
            "/wsService/stringPOST",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    testComplete();
                  });

            }).putHeader("content-length", String.valueOf("hello".getBytes().length)).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.write("hello");
    request.end();
    await();


  }


  @Test
  public void stringPOSTResponse() throws InterruptedException, ExecutionException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.post(
            "/wsService/stringPOSTResponse",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Assert.assertEquals(body.toString(), "hello");
                    testComplete();
                  });

            }).putHeader("content-length", String.valueOf("hello".getBytes().length)).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.write("hello");
    request.end();
    await();



  }

  @Test
  public void stringOPTIONSResponse() throws InterruptedException, ExecutionException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.options(
            "/wsService/stringOPTIONSResponse",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Assert.assertEquals(body.toString(), "hello");
                    testComplete();
                  });

            }).putHeader("Content-Type", "application/json;charset=UTF-8");

    request.end();
    await();



  }

  @Test
  public void stringPUTResponse() throws InterruptedException, ExecutionException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.put(
            "/wsService/stringPUTResponse",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Assert.assertEquals(body.toString(), "hello");
                    testComplete();
                  });

            }).putHeader("content-length", String.valueOf("hello".getBytes().length)).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.write("hello");
    request.end();
    await();


  }

  @Test
  public void stringDELETEResponse() throws InterruptedException, ExecutionException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.delete(
            "/wsService/stringDELETEResponse",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Assert.assertEquals(body.toString(), "hello");
                    testComplete();
                  });

            }).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.end();
    await();

  }

  @Test
  public void stringGETResponseWithParameter() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseWithParameter/123",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    assertEquals(body.toString(), "123");
                    testComplete();
                  });

            }).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.end();
    await();


  }

  @Test
  public void stringPOSTResponseWithParameter() throws InterruptedException, ExecutionException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.post(
            "/wsService/stringPOSTResponseWithParameter/123",
            resp -> {
              resp.exceptionHandler(error -> {

              });
              resp.bodyHandler(
                  body -> {
                    System.out.println("Got a createResponse: " + body.toString());
                    Assert.assertEquals(body.toString(), "hello:123");
                    testComplete();
                  });

            }).putHeader("content-length", String.valueOf("hello".getBytes().length)).putHeader("Content-Type", "application/json;charset=UTF-8");
    request.write("hello");
    request.end();
    await();

  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Path("/stringGETResponse")
    @GET
    public void rsstringGETResponse(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> future.complete("test")).execute();
    }

    @Path("/stringPOSTResponse")
    @POST
    public void rsstringPOSTResponse(RestHandler handler) {

      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringPOSTResponse: " + val);
      handler.response().stringResponse((future) -> future.complete(val)).execute();
    }

    @Path("/stringOPTIONSResponse")
    @OPTIONS
    public void rsstringOPTIONSResponse(RestHandler handler) {

      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringOPTIONSResponse: " + val);
      handler.response().stringResponse((future) -> future.complete("hello")).execute();
    }

    @Path("/stringPUTResponse")
    @PUT
    public void rsstringPUTResponse(RestHandler handler) {

      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringPUTResponse: " + val);
      handler.response().stringResponse((future) -> future.complete(val)).execute();
    }

    @Path("/stringDELETEResponse")
    @DELETE
    public void rsstringDELETEResponse(RestHandler handler) {

      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringDELETEResponse: " + val);
      handler.response().stringResponse((future) -> future.complete("hello")).execute();
    }

    @Path("/stringPOST")
    @POST
    public void rsstringPOST(RestHandler handler) {

      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringPOST: " + val);
      handler.response().end();
    }

    @Path("/stringPOSTNoEnd")
    @POST
    public void rsstringPOSTNoEnd(RestHandler handler) {

      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringPOSTNoEnd: " + val);
    }

    @Path("/stringGETResponseWithParameter/:help")
    @GET
    public void rsstringGETResponseWithParameter(RestHandler handler) {
      String productType = handler.request().param("help");
      System.out.println("wsEndpointTwo: " + handler);
      handler.response().stringResponse((future) -> future.complete(productType)).execute();
    }

    @Path("/stringPOSTResponseWithParameter/:help")
    @POST
    public void rsstringPOSTResponseWithParameter(RestHandler handler) {
      String productType = handler.request().param("help");
      String val = handler.request().body().getString(0, handler.request().body().length());
      System.out.println("stringPOSTResponse: " + val + ":" + productType);
      handler
          .response()
          .stringResponse((future) -> future.complete(val + ":" + productType))
          .execute();
    }
  }
}
