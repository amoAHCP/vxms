package org.jacpfx.eventbus;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.vxms.event.annotation.Consume;
import org.jacpfx.vxms.event.response.EventbusHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class EventbusBasicTests extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
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

    CountDownLatch latch2 = new CountDownLatch(1);
    DeploymentOptions options = new DeploymentOptions().setInstances(1);
    options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
    // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
    // don'failure have to hardecode it in your tests
    WsServiceOne one = new WsServiceOne();
    one.init(vertx, vertx.getOrCreateContext());
    getVertx().deployVerticle(one, options, asyncResult -> {
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

  public void simpleBlockingStringResponse() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleBlockingStringResponse", "hello", res -> {
      assertTrue(res.succeeded());
      assertEquals("hello", res.result().body().toString());
      System.out.println("out: " + res.result().body().toString());
      testComplete();
    });
    await();

  }

  @Test

  public void simpleStringResponse() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleStringResponse", "hello", res -> {
      assertTrue(res.succeeded());
      assertEquals("hello", res.result().body().toString());
      System.out.println("out: " + res.result().body().toString());
      testComplete();
    });
    await();

  }

  @Test

  public void simpleByteResponse() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleByteResponse", "hello", res -> {
      assertTrue(res.succeeded());
      Payload<String> pp = null;
      final Object body = res.result().body();
      try {
        pp = (Payload<String>) Serializer.deserialize((byte[]) body);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      String value = pp.getValue();
      assertEquals("hello", value);
      System.out.println("out: " + value);
      testComplete();
    });
    await();

  }

  @Test

  public void simpleBlockingByteResponse() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleBlockingByteResponse", "hello", res -> {
      assertTrue(res.succeeded());
      Payload<String> pp = null;
      final Object body = res.result().body();
      try {
        pp = (Payload<String>) Serializer.deserialize((byte[]) body);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      String value = pp.getValue();
      assertEquals("hello", value);
      System.out.println("out: " + value);
      testComplete();
    });
    await();

  }

  @Test
  public void simpleObjectResponse() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleObjectResponse", "hello", res -> {
      assertTrue(res.succeeded());
      Payload<String> pp = null;
      final Object body = res.result().body();
      try {
        pp = (Payload<String>) Serializer.deserialize((byte[]) body);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      String value = pp.getValue();
      assertEquals("hello", value);
      System.out.println("out: " + value);
      testComplete();
    });
    await();

  }

  @Test
  public void simpleBlockingObjectResponse() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleBlockingObjectResponse", "hello", res -> {
      assertTrue(res.succeeded());
      Payload<String> pp = null;
      final Object body = res.result().body();
      try {
        pp = (Payload<String>) Serializer.deserialize((byte[]) body);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      String value = pp.getValue();
      assertEquals("hello", value);
      System.out.println("out: " + value);
      testComplete();
    });
    await();

  }

  public HttpClient getClient() {
    return client;
  }


  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Consume("/simpleStringResponse")
    public void simpleStringResponse(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply.response()
          .stringResponse((future) -> future.complete(reply.request().body().toString())).execute();
    }

    @Consume("/simpleBlockingStringResponse")
    public void simpleBlockingStringResponse(EventbusHandler reply) {
      System.out.println("simpleStringResponse: " + reply);
      reply.response().blocking().stringResponse(() -> reply.request().body().toString()).execute();
    }


    @Consume("/simpleByteResponse")
    public void simpleByteResponse(EventbusHandler reply) {
      System.out.println("simpleByteResponse: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      reply.response().byteResponse((future) -> future.complete(Serializer.serialize(p))).execute();
    }

    @Consume("/simpleBlockingByteResponse")
    public void simpleBlockingByteResponse(EventbusHandler reply) {
      System.out.println("simpleByteResponse: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      reply.response().blocking().byteResponse(() -> Serializer.serialize(p)).execute();
    }

    @Consume("/simpleObjectResponse")
    public void simpleObjectResponse(EventbusHandler reply) {
      System.out.println("simpleByteResponse: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      reply.response().objectResponse((future) -> future.complete(p), new ExampleByteEncoder())
          .execute();
    }

    @Consume("/simpleBlockingObjectResponse")
    public void simpleBlockingObjectResponse(EventbusHandler reply) {
      System.out.println("simpleByteResponse: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      reply.response().blocking().objectResponse(() -> p, new ExampleByteEncoder()).execute();
    }

  }


}
