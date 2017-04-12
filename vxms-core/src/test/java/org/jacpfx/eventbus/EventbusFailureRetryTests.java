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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.vertx.event.annotation.Consume;
import org.jacpfx.vertx.event.response.EventbusHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class EventbusFailureRetryTests extends VertxTestBase {

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

  public void simpleStringResponseFailure() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleStringResponseFailure", "hello", res -> {
      assertTrue(res.succeeded());
      assertEquals("helloNPE4", res.result().body().toString());
      System.out.println("out: " + res.result().body().toString());
      testComplete();
    });
    await();

  }

  @Test

  public void simpleBlockingStringResponseFailure() throws InterruptedException {
    getVertx().eventBus()
        .send(SERVICE_REST_GET + "/simpleBlockingStringResponseFailure", "hello", res -> {
          assertTrue(res.succeeded());
          assertEquals("helloNPE4", res.result().body().toString());
          System.out.println("out: " + res.result().body().toString());
          testComplete();
        });
    await();

  }


  @Test

  public void simpleStringResponseFailureInCompleate() throws InterruptedException {
    getVertx().eventBus()
        .send(SERVICE_REST_GET + "/simpleStringResponseFailureInCompleate", "hello", res -> {
          assertTrue(res.succeeded());
          assertEquals("helloNPE4", res.result().body().toString());
          System.out.println("out: " + res.result().body().toString());
          testComplete();
        });
    await();

  }

  @Test

  public void simpleByteResponseFailure() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleByteResponseFailure", "hello", res -> {
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
      assertEquals("helloNPE4", value);
      System.out.println("out: " + value);
      testComplete();
    });
    await();

  }

  @Test

  public void simpleBlockingByteResponseFailure() throws InterruptedException {
    getVertx().eventBus()
        .send(SERVICE_REST_GET + "/simpleBlockingByteResponseFailure", "hello", res -> {
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
          assertEquals("helloNPE4", value);
          System.out.println("out: " + value);
          testComplete();
        });
    await();

  }

  @Test
  public void simpleObjectResponseFailure() throws InterruptedException {
    getVertx().eventBus().send(SERVICE_REST_GET + "/simpleObjectResponseFailure", "hello", res -> {
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
      assertEquals("helloNPE4", value);
      System.out.println("out: " + value);
      testComplete();
    });
    await();

  }


  @Test
  public void simpleObjectResponseFailureInCompleate() throws InterruptedException {
    getVertx().eventBus()
        .send(SERVICE_REST_GET + "/simpleObjectResponseFailureInCompleate", "hello", res -> {
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
          assertEquals("helloNPE4", value);
          System.out.println("out: " + value);
          testComplete();
        });
    await();

  }

  @Test
  public void simpleBlockingObjectResponseFailure() throws InterruptedException {
    getVertx().eventBus()
        .send(SERVICE_REST_GET + "/simpleBlockingObjectResponseFailure", "hello", res -> {
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
          assertEquals("helloNPE4", value);
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

    @Consume("/simpleStringResponseFailure")
    public void simpleStringResponseFailure(EventbusHandler reply) {
      System.out.println("simpleStringResponseFailure: " + reply);
      AtomicInteger count = new AtomicInteger(0);
      reply.response().stringResponse((future) -> {
        final String body = reply.request().body().toString() + "NPE";
        count.incrementAndGet();
        throw new NullPointerException(body);
      }).retry(3).onFailureRespond((t, f) -> f.complete(t.getMessage() + count.get())).execute();
    }

    @Consume("/simpleStringResponseFailureInCompleate")
    public void simpleStringResponseFailureInCompleate(EventbusHandler reply) {
      System.out.println("simpleStringResponseFailureInCompleate: " + reply);
      AtomicInteger count = new AtomicInteger(0);
      reply.response().stringResponse((future) -> {
        final String body = reply.request().body().toString() + "NPE";
        count.incrementAndGet();
        Supplier<String> s = () -> {
          throw new NullPointerException(body);
        };
        future.complete(s.get());
      }).retry(3).onFailureRespond((t, f) -> f.complete(t.getMessage() + count.get())).execute();
    }

    @Consume("/simpleBlockingStringResponseFailure")
    public void simpleBlockingStringResponseFailure(EventbusHandler reply) {
      System.out.println("simpleBlockingStringResponseFailure: " + reply);
      AtomicInteger count = new AtomicInteger(0);
      reply.response().blocking().stringResponse(() -> {
        count.incrementAndGet();
        throw new NullPointerException(reply.request().body().toString() + "NPE");
      }).
          retry(3)
          .onFailureRespond(t -> {
            return t.getMessage() + count.get();
          }).execute();
    }


    @Consume("/simpleByteResponseFailure")
    public void simpleByteResponseFailure(EventbusHandler reply) {
      System.out.println("simpleByteResponseFailure: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      AtomicInteger count = new AtomicInteger(0);
      reply.response().byteResponse((future) -> {
        final byte[] serialize = Serializer.serialize(p);
        count.incrementAndGet();
        throw new NullPointerException(reply.request().body().toString());
      }).retry(3).onFailureRespond((t, f) -> f
          .complete(Serializer.serialize(new Payload<>(t.getMessage() + "NPE" + count.get()))))
          .execute();
    }

    @Consume("/simpleByteResponseFailureInCompleate")
    public void simpleByteResponseFailureInCompleate(EventbusHandler reply) {
      System.out.println("simpleByteResponseFailureInCompleate: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      AtomicInteger count = new AtomicInteger(0);
      reply.response().byteResponse((future) -> {
        final byte[] serialize = Serializer.serialize(p);
        count.incrementAndGet();
        Supplier<byte[]> s = () -> {
          throw new NullPointerException(reply.request().body().toString());
        };
        future.complete(s.get());
      }).retry(3).onFailureRespond((t, f) -> f
          .complete(Serializer.serialize(new Payload<>(t.getMessage() + "NPE" + count.get()))))
          .execute();
    }

    @Consume("/simpleBlockingByteResponseFailure")
    public void simpleBlockingByteResponseFailure(EventbusHandler reply) {
      System.out.println("simpleByteResponseFailure: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      AtomicInteger count = new AtomicInteger(0);
      reply.response().blocking().byteResponse(() -> {
        count.incrementAndGet();
        throw new NullPointerException(reply.request().body().toString() + "NPE");
      }).retry(3)
          .onFailureRespond(t -> Serializer.serialize(new Payload<>(t.getMessage() + count.get())))
          .execute();
    }

    @Consume("/simpleObjectResponseFailure")
    public void simpleObjectResponseFailure(EventbusHandler reply) {
      System.out.println("simpleObjectResponseFailure: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      AtomicInteger count = new AtomicInteger(0);
      reply.response().objectResponse((future) -> {
        count.incrementAndGet();
        throw new NullPointerException(reply.request().body().toString());
      }, new ExampleByteEncoder()).retry(3).onFailureRespond(
          (t, f) -> f.complete(new Payload<>(t.getMessage() + "NPE" + count.get())),
          new ExampleByteEncoder()).execute();
    }


    @Consume("/simpleObjectResponseFailureInCompleate")
    public void simpleObjectResponseFailureInCompleate(EventbusHandler reply) {
      System.out.println("simpleObjectResponseFailure: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      AtomicInteger count = new AtomicInteger(0);
      reply.response().objectResponse((future) -> {
        count.incrementAndGet();
        Supplier<Payload<String>> s = () -> {
          throw new NullPointerException(reply.request().body().toString());
        };
        future.complete(s.get());
      }, new ExampleByteEncoder()).retry(3).onFailureRespond(
          (t, f) -> f.complete(new Payload<>(t.getMessage() + "NPE" + count.get())),
          new ExampleByteEncoder()).execute();
    }

    @Consume("/simpleBlockingObjectResponseFailure")
    public void simpleBlockingObjectResponseFailure(EventbusHandler reply) {
      System.out.println("simpleByteResponseFailure: " + reply);
      Payload<String> p = new Payload<>(reply.request().body().toString());
      AtomicInteger count = new AtomicInteger(0);
      reply.response().blocking().objectResponse(() -> {
        count.incrementAndGet();
        throw new NullPointerException(reply.request().body().toString());
      }, new ExampleByteEncoder()).retry(3)
          .onFailureRespond(t -> new Payload<>(t.getMessage() + "NPE" + count.get()),
              new ExampleByteEncoder()).execute();
    }

  }


}
