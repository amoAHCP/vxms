package org.jacpfx.circuitbreaker;


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
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientCircuitBrakerTests extends VertxTestBase {

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

    getVertx().deployVerticle(new WsServiceOne(), options, asyncResult -> {
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
  @Ignore
  // TODO implement basic test for String, Byte and Object...
  public void stringGETResponseCircuitBaseTest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/stringGETResponseCircuitBaseTest");
    Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async()
        .get(new InvocationCallback<String>() {

          @Override
          public void completed(String response) {
            System.out.println("Response entity '" + response + "' received.");
            vertx.runOnContext(h -> {
              System.out.println("--------");
              assertEquals(response, "xyz");
              System.out.println("----ccc----");
              testComplete();
            });
          }

          @Override
          public void failed(Throwable throwable) {
            System.out.println("fffffff");
          }
        });

    //  latch.await();
    //  testComplete();

  }


  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    /////------------- sync blocking ----------------

    @Path("/stringGETResponseCircuitBaseTest")
    @GET
    public void stringGETResponseCircuitBaseTest(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> {
        throw new NullPointerException("test-123");
      }).execute();
    }


  }

}
