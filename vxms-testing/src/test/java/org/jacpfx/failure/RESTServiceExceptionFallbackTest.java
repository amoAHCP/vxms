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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.annotation.OnRestError;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTServiceExceptionFallbackTest extends VertxTestBase {

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

    awaitLatch(latch2);
  }

  @Test
  public void exceptionInStringResponseWithErrorHandler() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInStringResponseWithErrorHandler?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------exceptionInStringResponse: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void exceptionInErrorHandler() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInErrorHandler",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------exceptionInStringResponse: " + val);
                      assertEquals(val, "catched");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Path("/exceptionInStringResponseWithErrorHandler")
    @GET
    public void rsexceptionInStringResponseWithErrorHandler(RestHandler handler) {
      handler
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("Test");
                // return "";
              })
          .execute();
    }

    @OnRestError("/exceptionInStringResponseWithErrorHandler")
    @GET
    public void errorMethod1(RestHandler handler, Throwable t) {
      System.out.println("+++++++rsexceptionInStringResponseWithErrorHandlerError: " + handler);
      t.printStackTrace();
      System.out.println("----------------------------------");
      throw new NullPointerException("test...1234");
    }

    @Path("/exceptionInErrorHandler")
    @GET
    public void exceptionInErrorHandler(RestHandler handler) {
      System.out.println("exceptionInErrorHandler: " + handler);
      handler
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("Test");
                // return "";
              })
          .onFailureRespond(
              (error, response) -> {
                throw new NullPointerException("Test2");
              })
          .execute();
    }

    @OnRestError("/exceptionInErrorHandler")
    @GET
    public void exceptionInErrorHandlerErrorHandlerError(RestHandler handler, Throwable t) {
      System.out.println("+++++++exceptionInErrorHandler: " + t.getMessage());
      t.printStackTrace();
      System.out.println("----------------------------------");
      handler.response().stringResponse(h -> h.complete("catched")).execute();
    }
  }
}
