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
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.entity.encoder.ExampleStringEncoder;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.annotation.OnRestError;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTServiceExceptionTest extends VertxTestBase {

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
  public void noResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/noResponse?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------noResponse: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });

    request.end();
    await();
    // request.end();
  }

  @Test
  public void exceptionInMethodBody() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInMethodBody?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------exceptionInMethodBody: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await();
  }

  @Test
  public void rsexceptionInMethodBodyWithErrorHandler() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInMethodBodyWithErrorHandler?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println(
                          "--------exceptionInMethodBody: "
                              + val
                              + " status:  "
                              + resp.statusCode());
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void exceptionInStringResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInStringResponse?val=123&tmp=456",
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
  public void exceptionInAsyncStringResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInAsyncStringResponse?val=123&tmp=456",
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
  public void exceptionInByteResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInByteResponse?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------exceptionInByteResponse: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void exceptionInObjectResponse() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInObjectResponse?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println(
                          "--------exceptionInObjectResponse: "
                              + val
                              + " resp:"
                              + resp.statusMessage()
                              + " code:"
                              + resp.statusCode());
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
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
  public void exceptionInAsyncStringResponseWithErrorHandler() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/exceptionInAsyncStringResponseWithErrorHandler?val=123&tmp=456",
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
  public void catchedAsyncStringErrorDelay() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/catchedAsyncStringErrorDelay?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------catchedAsyncStringErrorDelay: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void catchedAsyncByteErrorDelay() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/catchedAsyncByteErrorDelay?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------catchedAsyncByteErrorDelay: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(5000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void catchedAsyncObjectErrorDelay() throws InterruptedException {
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);

    HttpClientRequest request =
        client.get(
            "/wsService/catchedAsyncObjectErrorDelay?val=123&tmp=456",
            new Handler<HttpClientResponse>() {
              public void handle(HttpClientResponse resp) {
                resp.bodyHandler(
                    body -> {
                      String val = body.getString(0, body.length());
                      System.out.println("--------catchedAsyncByteErrorDelay: " + val);
                      // assertEquals(key, "val");
                      testComplete();
                    });
              }
            });
    request.end();

    await(10000, TimeUnit.MILLISECONDS);
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Path("/noResponse")
    @GET
    public void rsNoResponse(RestHandler handler) {
      handler.response().end();
      System.out.println("rsNoResponse: " + handler);
    }

    @Path("/exceptionInMethodBody")
    @GET
    public void rsexceptionInMethodBody(RestHandler handler) {
      System.out.println("rsexceptionInMethodBody: " + handler);
      throw new NullPointerException("test");
    }

    @Path("/exceptionInMethodBodyWithErrorHandler")
    @GET
    public void rsexceptionInMethodBodyWithErrorHandler(RestHandler handler) {
      System.out.println("rsexceptionInMethodBody: " + handler);
      throw new NullPointerException("test");
    }

    @OnRestError("/exceptionInMethodBodyWithErrorHandler")
    @GET
    public void rsexceptionInMethodBodyWithErrorHandlerError(RestHandler handler, Throwable t) {
      System.out.println("jgf4234354354: " + handler);
      t.printStackTrace();
      System.out.println("----------------------------------");
      throw new NullPointerException("test...1234");
    }

    @Path("/exceptionInStringResponse")
    @GET
    public void rsexceptionInStringResponse(RestHandler handler) {
      System.out.println("rsexceptionInMethodBody: " + handler);
      handler
          .response()
          .stringResponse(
              (future) -> {
                throw new NullPointerException("Test");
                // return "";
              })
          .execute();
    }

    @Path("/exceptionInAsyncStringResponse")
    @GET
    public void rsexceptionInAsyncStringResponse(RestHandler handler) {
      System.out.println("exceptionInAsyncStringResponse: " + handler);
      handler
          .response()
          .blocking()
          .stringResponse(
              () -> {
                throw new NullPointerException("Test");
                // return "";
              })
          .execute();
    }

    @Path("/exceptionInObjectResponse")
    @GET
    public void rsexceptionInObjectResponse(RestHandler handler) {
      System.out.println("rsexceptionInObjectResponse: " + handler);
      handler
          .response()
          .objectResponse(
              (future) -> {
                System.out.println("Exception-->>>>>><");
                throw new NullPointerException("Test");
                // return "";
              },
              new ExampleByteEncoder())
          .execute();
    }

    @Path("/exceptionInByteResponse")
    @GET
    public void rsexceptionInByteResponse(RestHandler handler) {
      System.out.println("exceptionInByteResponse: " + handler);
      handler
          .response()
          .byteResponse(
              (future) -> {
                throw new NullPointerException("Test");
                // return "";
              })
          .execute();
    }

    @Path("/exceptionInStringResponseWithErrorHandler")
    @GET
    public void rsexceptionInStringResponseWithErrorHandler(RestHandler handler) {
      System.out.println("exceptionInStringResponseWithErrorHandler: " + handler);
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
    public void rsexceptionInStringResponseWithErrorHandlerError(RestHandler handler, Throwable t) {
      System.out.println("+++++++rsexceptionInStringResponseWithErrorHandlerError: " + handler);
      t.printStackTrace();
      System.out.println("----------------------------------");
      throw new NullPointerException("test...1234");
    }

    @Path("/exceptionInAsyncStringResponseWithErrorHandler")
    @GET
    public void rsexceptionAsyncInStringResponseWithErrorHandler(RestHandler handler) {
      System.out.println("exceptionInStringResponseWithErrorHandler: " + handler);
      handler
          .response()
          .blocking()
          .stringResponse(
              () -> {
                throw new NullPointerException("Test");
                // return "";
              })
          .execute();
    }

    @OnRestError("/exceptionInAsyncStringResponseWithErrorHandler")
    @GET
    public void exceptionInAsyncStringResponseWithErrorHandlerError(
        RestHandler handler, Throwable t) {
      System.out.println("+++++++rsexceptionInAsyncStringResponseWithErrorHandler: " + handler);
      t.printStackTrace();
      System.out.println("----------------------------------");
      throw new NullPointerException("test...1234");
    }

    @Path("/catchedAsyncStringErrorDelay")
    @GET
    public void rscatchedAsyncStringErrorDelay(RestHandler reply) {
      long startTime = System.currentTimeMillis();
      AtomicInteger count = new AtomicInteger(4);
      reply
          .response()
          .blocking()
          .stringResponse(
              () -> {
                long estimatedTime = System.currentTimeMillis() - startTime;
                System.out.println("time: " + estimatedTime);
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw");
                  throw new NullPointerException("test");
                }

                return null;
              })
          .retry(3)
          .delay(1000)
          .onFailureRespond(
              (t) -> {
                System.out.print("the stack trace --> ");
                t.printStackTrace();
                return "hello world";
              })
          .execute();
    }

    @Path("/catchedAsyncByteErrorDelay")
    @GET
    public void rscatchedAsyncByteErrorDelay(RestHandler reply) {
      long startTime = System.currentTimeMillis();
      AtomicInteger count = new AtomicInteger(4);
      reply
          .response()
          .blocking()
          .byteResponse(
              () -> {
                long estimatedTime = System.currentTimeMillis() - startTime;
                System.out.println("time: " + estimatedTime);
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw");
                  throw new NullPointerException("test");
                }

                return null;
              })
          .retry(3)
          .delay(1000)
          .onFailureRespond(
              (t) -> {
                System.out.print("the stack trace --> ");
                t.printStackTrace();
                return "hello world".getBytes();
              })
          .execute();
    }

    @Path("/catchedAsyncObjectErrorDelay")
    @GET
    public void rscatchedAsyncObjectErrorDelay(RestHandler reply) {
      long startTime = System.currentTimeMillis();
      AtomicInteger count = new AtomicInteger(4);
      reply
          .response()
          .blocking()
          .objectResponse(
              () -> {
                long estimatedTime = System.currentTimeMillis() - startTime;
                System.out.println("time: " + estimatedTime);
                if (count.decrementAndGet() >= 0) {
                  System.out.println("throw");
                  throw new NullPointerException("test");
                }

                return null;
              },
              new ExampleStringEncoder())
          .retry(3)
          .delay(1000)
          .onFailureRespond(
              (t) -> {
                System.out.print("the stack trace --> ");
                t.printStackTrace();
                return new Payload<String>("hello world");
              },
              new ExampleStringEncoder())
          .execute();
    }
  }
}
