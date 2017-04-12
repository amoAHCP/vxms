package org.jacpfx.evbbridge;


import io.vertx.core.AbstractVerticle;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientEventStringResponseTest extends VertxTestBase {

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

    getVertx().deployVerticle(new WsServiceTwo(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });
    getVertx().deployVerticle(new TestVerticle(), options, asyncResult -> {
      // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
      System.out.println("start service: " + asyncResult.succeeded());
      assertTrue(asyncResult.succeeded());
      assertNotNull("deploymentID should not be null", asyncResult.result());
      // If deployed correctly then start the tests!
      //   latch2.countDown();

      latch2.countDown();

    });
    getVertx().deployVerticle(new TestErrorVerticle(), options, asyncResult -> {
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

  public void complexSyncResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT2)
        .path("/wsService/complexSyncResponse");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            String value = response;
            vertx.runOnContext(h -> {

              assertEquals(value, "hello1");
            });
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

  public void complexSyncErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT2)
        .path("/wsService/complexSyncErrorResponse");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            Assert.assertEquals(response, "test exception");
            String value = response;
            vertx.runOnContext(h -> {

              assertEquals(value, "test exception");
            });
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

  public void simpleSyncNoConnectionErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT2)
        .path("/wsService/simpleSyncNoConnectionErrorResponse");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            String value = response;
            vertx.runOnContext(h -> {

              assertEquals(value, "No handlers for address hello1");
            });
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
  @Ignore
  public void simpleSyncNoConnectionErrorTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT2)
        .path("/wsService/simpleSyncNoConnectionError");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            String value = response;
            vertx.runOnContext(h -> {

              assertEquals(value, "no connection");
            });
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

  public void simpleSyncNoConnectionRetryErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:" + PORT2)
        .path("/wsService/simpleSyncNoConnectionRetryErrorResponse");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            String value = response;
            vertx.runOnContext(h -> {

              assertEquals(value, "hello1");
            });
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

  public void simpleSyncNoConnectionExceptionRetryErrorResponseTest() throws InterruptedException {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT2)
        .path("/wsService/simpleSyncNoConnectionExceptionRetryErrorResponse");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            String value = response;
            vertx.runOnContext(h -> {

              assertEquals(value, "hello1");
            });
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


  public HttpClient getClient() {
    return client;
  }


  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT2)
  public class WsServiceTwo extends VxmsEndpoint {


    @Path("/complexSyncResponse")
    @GET
    public void complexSyncResponse(RestHandler reply) {
      System.out.println("CALL");
      reply.
          eventBusRequest().
          send("hello", "welt").
          mapToStringResponse((handler, future) ->
              future.complete(handler.
                  result().
                  body().toString() + "1")).
          execute();
    }


    @Path("/complexSyncErrorResponse")
    @GET
    public void complexSyncErrorResponse(RestHandler reply) {
      reply.eventBusRequest().
          send("hello", "welt").
          mapToStringResponse((handler, future) -> {
            throw new NullPointerException("test exception");
          }).
          onFailureRespond((error, response) -> response.complete(error.getMessage())).
          execute();
    }

    @Path("/simpleSyncNoConnectionErrorResponse")
    @GET
    public void simpleSyncNoConnectionErrorResponse(RestHandler reply) {
      System.out.println("-------1");
      reply.eventBusRequest().
          send("hello1", "welt").
          mapToStringResponse((handler, future) -> {
            System.out.println("value from event  ");
            future.complete(handler.result().body().toString());
          }).
          onError(error -> {
            System.out.println(":::" + error.getMessage());
          }).
          retry(3).
          onFailureRespond((t, c) -> c.complete(t.getMessage())).
          execute();
      System.out.println("-------2");
    }

    @Path("/simpleSyncNoConnectionError")
    @GET
    public void simpleSyncNoConnectionError(RestHandler reply) {
      reply.eventBusRequest().
          send("hello1", "welt").
          mapToStringResponse((handler, future) ->
              future.complete(handler.result().body()
                  .toString())).//onFailure(error-> System.out.println("ERROR:"+error.getMessage()+"type: "+error.getClass())).
          execute();
    }

    @Path("/simpleSyncNoConnectionRetryErrorResponse")
    @GET
    public void simpleSyncNoConnectionRetryErrorResponse(RestHandler reply) {
      reply.eventBusRequest().
          send("error", "1").
          mapToStringResponse((handler, future) ->
              future.complete(handler.result().body().toString() + "1")).
          retry(3).
          execute();
    }

    @Path("/simpleSyncNoConnectionExceptionRetryErrorResponse")
    @GET
    public void simpleSyncNoConnectionExceptionRetryErrorResponse(RestHandler reply) {
      AtomicInteger count = new AtomicInteger(0);
      reply.eventBusRequest().
          send("hello", "welt").
          mapToStringResponse((handler, future) -> {
            System.out.println("retry: " + count.get());
              if (count.incrementAndGet() < 3) {
                  throw new NullPointerException("test");
              }
            future.complete(handler.result().body().toString() + "1");
          }).
          retry(3).
          execute();
    }


  }


  public class TestVerticle extends AbstractVerticle {

    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      System.out.println("start");
      vertx.eventBus().consumer("hello", handler -> {
        System.out.println("request::" + handler.body().toString());
        handler.reply("hello");
      });
      startFuture.complete();
    }
  }

  public class TestErrorVerticle extends AbstractVerticle {

    private AtomicLong counter = new AtomicLong(0L);

    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      System.out.println("start");
      vertx.eventBus().consumer("error", handler -> {
        System.out.println("request::" + handler.body().toString());
        if (counter.incrementAndGet() % 3 == 0) {
          System.out.println("reply::" + handler.body().toString());
          handler.reply("hello");
        } else {
          System.out.println("fail::" + handler.body().toString());
          handler.fail(500, "my error");
        }

      });
      startFuture.complete();
    }
  }


}
