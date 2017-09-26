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
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.jacpfx.entity.RestrictedCorsRouterConfig;
import org.jacpfx.entity.RestrictedCorsRouterConfig2;
import org.jacpfx.entity.RestrictedCorsRouterConfig3;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientCORSTest extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  public static final int PORT2 = 9999;
  public static final int PORT3 = 9991;
  private final static int MAX_RESPONSE_ELEMENTS = 4;
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
    // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
    // don'failure have to hardecode it in your tests

    getVertx().deployVerticle(new WsServiceOne(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });

    getVertx().deployVerticle(new WsServiceTwo(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });

    getVertx().deployVerticle(new WsServiceThree(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });

    client = getVertx().
        createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);

  }


  @Test
  public void corsFail() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/stringGETResponseSyncAsync");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header("Origin", "http://jacpfx.org").async().get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("should not be called::::" + System
                .getProperty("sun.net.http.allowRestrictedHeaders"));
          }

          @Override
          public void failed(Throwable throwable) {
            System.out.println(throwable.getMessage());
            Assert.assertEquals(
                "javax.ws.rs.ForbiddenException: HTTP 403 CORS Rejected - Invalid origin",
                throwable.getMessage());
            latch.countDown();
          }
        });

    latch.await();
    testComplete();

  }

  @Test
  public void corsOK() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT2)
        .path("/wsService/stringGETResponseSyncAsync");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header("Origin", "http://example.com").async().get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            Assert.assertEquals(response, "test-123");
            latch.countDown();
          }

          @Override
          public void failed(Throwable throwable) {
            throwable.printStackTrace();
          }
        });

    latch.await();
    testComplete();

  }


  @Test
  public void WsServiceThree() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT3)
        .path("/wsService/stringGETResponseSyncAsync");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header("Origin", "http://example.com").async().get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            Assert.assertEquals(response, "test-123");
            latch.countDown();
          }

          @Override
          public void failed(Throwable throwable) {
            throwable.printStackTrace();
          }
        });

    latch.await();
    testComplete();

  }

  @Test
  public void WsServiceThree_1() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT3)
        .path("/wsService/stringGETResponseSyncAsync2");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header("Origin", "http://example1.com").async().get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("MESSAGE: " + response);
          }

          @Override
          public void failed(Throwable throwable) {
            System.out.println(throwable.getMessage());
            Assert.assertEquals(
                "javax.ws.rs.ForbiddenException: HTTP 403 CORS Rejected - Invalid origin",
                throwable.getMessage());
            latch.countDown();
          }
        });

    latch.await();
    testComplete();

  }


  public HttpClient getClient() {
    return client;
  }

  // TODO extend test for POST, OPTIONAL,....
  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT, routerConf = RestrictedCorsRouterConfig.class)
  public class WsServiceOne extends VxmsEndpoint {

    /////------------- sync blocking ----------------

    @Path("/stringGETResponseSyncAsync")
    @GET
    public void rsstringGETResponseSyncAsync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> {
        future.complete("xxxx");
      }).execute();
    }

  }


  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT2, routerConf = RestrictedCorsRouterConfig2.class)
  public class WsServiceTwo extends VxmsEndpoint {

    /////------------- sync blocking ----------------

    @Path("/stringGETResponseSyncAsync")
    @GET
    public void rsstringGETResponseSyncAsync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> {
        future.complete("test-123");
      }).execute();
    }

  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT3, routerConf = RestrictedCorsRouterConfig3.class)
  public class WsServiceThree extends VxmsEndpoint {

    /////------------- sync blocking ----------------

    @Path("/stringGETResponseSyncAsync")
    @GET
    public void rsstringGETResponseSyncAsync(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> {
        future.complete("test-123");
      }).execute();
    }

    @Path("/stringGETResponseSyncAsync2")
    @GET
    public void rsstringGETResponseSyncAsync2(RestHandler reply) {
      System.out.println("stringResponse xyz: " + reply);
      reply.response().stringResponse((future) -> {
        future.complete("test-123");
      }).execute();
    }

  }

}
