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

package org.jacpfx.postconstruct;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class StaticPostConstructTest extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  public static final int PORT_2 = 9988;
  public static final int PORT_3 = 9999;
  public static final String POST_VAL = "post";
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
              resp.bodyHandler(
                  body -> {
                    assertEquals(body.toString(), POST_VAL);
                    testComplete();
                  });
            });
    request.end();
    await();
  }

  @Test
  public void stringGETResponse2() throws InterruptedException {

    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT_2);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/stringGETResponse",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    assertEquals(body.toString(), POST_VAL);
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

    String postVal;

    @Override
    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      VxmsEndpoint.start(startFuture, this);
    }

    public void postConstruct(final io.vertx.core.Future<Void> startFuture) {
      postVal = POST_VAL;
      startFuture.complete();
    }

    @Path("/stringGETResponse")
    @GET
    public void rsstringGETResponse(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> future.complete(postVal)).execute();
    }
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT_2)
  public class WsServiceTwo extends AbstractVerticle {

    String postVal;

    @Override
    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      VxmsEndpoint.start(startFuture, this);
    }

    public void postConstruct(Router router, final io.vertx.core.Future<Void> startFuture) {
      postVal = POST_VAL;
      startFuture.complete();
    }

    @Path("/stringGETResponse")
    @GET
    public void rsstringGETResponse(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> future.complete(postVal)).execute();
    }
  }
}
