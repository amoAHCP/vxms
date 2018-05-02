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

package org.jacpfx.failure;

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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.annotation.OnRestError;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTJerseyClientErrorTests extends VertxTestBase {

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
  public void stringGETResponseSyncAsync() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseSyncAsync",
            resp -> resp.bodyHandler(
                body -> {
                  String val = body.getString(0, body.length());
                  assertEquals("test-123", val);
                  testComplete();
                }));
    request.end();

    await(10000, TimeUnit.MILLISECONDS);

  }

  @Test
  public void stringGETResponseAsyncSync() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseAsyncSync",
            resp -> resp.bodyHandler(
                body -> {
                  String val = body.getString(0, body.length());
                  assertEquals("test-123", val);
                  testComplete();
                }));
    request.end();

    await(10000, TimeUnit.MILLISECONDS);


  }

  @Test
  public void stringGETResponseAsyncAsync() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseAsyncAsync",
            resp -> resp.bodyHandler(
                body -> {
                  String val = body.getString(0, body.length());
                  assertEquals("test-123", val);
                  testComplete();
                }));
    request.end();

    await(10000, TimeUnit.MILLISECONDS);


  }

  @Test
  public void stringGETResponseSyncSync() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponseSyncSync",
            resp -> resp.bodyHandler(
                body -> {
                  String val = body.getString(0, body.length());
                  assertEquals("test-123", val);
                  testComplete();
                }));
    request.end();

    await(10000, TimeUnit.MILLISECONDS);



  }

  public HttpClient getClient() {
    return client;
  }

  // TODO extend test for POST, OPTIONAL,....
  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Path("/circuitBreakerTest1")
    @GET
    public void circuitBreakerTest1(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("test-123");
              })
          .retry(3)
          .execute();
    }

    ///// ------------- sync blocking ----------------

    @Path("/stringGETResponseSyncAsync")
    @GET
    public void rsstringGETResponseSyncAsync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("test-123");
              })
          .execute();
    }

    @OnRestError("/stringGETResponseSyncAsync")
    @GET
    public void rsstringGETResponseSyncAsyncError(Throwable t, RestHandler handler) {
      System.out.println("ERROR stringGETResponseSyncAsync");
      handler.response().blocking().stringResponse(() -> t.getMessage()).execute();
    }

    ///// ------------- sync blocking  END----------------

    ///// ------------- blocking sync ----------------

    @Path("/stringGETResponseAsyncSync")
    @GET
    public void rsstringGETResponseAsyncSync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                throw new NullPointerException("test-123");
              })
          .execute();
    }

    @OnRestError("/stringGETResponseAsyncSync")
    @GET
    public void rsstringGETResponseAsyncSyncError(Throwable t, RestHandler handler) {
      System.out.println("ERROR stringGETResponseAsyncSync");
      handler.response().stringResponse((future) -> future.complete(t.getMessage())).execute();
    }
    ///// ------------- blocking sync  END----------------

    ///// ------------- blocking blocking ----------------

    @Path("/stringGETResponseAsyncAsync")
    @GET
    public void rsstringGETResponseAsyncAsync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                throw new NullPointerException("test-123");
              })
          .execute();
    }

    @OnRestError("/stringGETResponseAsyncAsync")
    @GET
    public void rsstringGETResponseAsyncAsyncError(Throwable t, RestHandler handler) {
      System.out.println("ERROR-- stringGETResponseAsyncAsync: " + t.getMessage());

      handler.response().blocking().stringResponse(() -> t.getMessage()).execute();
    }

    ///// ------------- blocking blocking END ----------------

    ///// ------------- sync sync ----------------

    @Path("/stringGETResponseSyncSync")
    @GET
    public void rsstringGETResponseSyncSync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("test-123");
              })
          .execute();
    }

    @OnRestError("/stringGETResponseSyncSync")
    @GET
    public void rsstringGETResponseSyncSyncError(Throwable t, RestHandler handler) {
      System.out.println("ERROR stringGETResponseSyncSync: " + t.getMessage());
      handler.response().stringResponse((future) -> future.complete(t.getMessage())).execute();
    }

    ///// ------------- blocking blocking END ----------------

  }
}
