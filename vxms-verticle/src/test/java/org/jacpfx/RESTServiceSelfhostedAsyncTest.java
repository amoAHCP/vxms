package org.jacpfx;


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
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleStringEncoder;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTServiceSelfhostedAsyncTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "localhost";
    public static final int PORT = 9090;

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


    private HttpClient client;

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
        // don't have to hardecode it in your tests

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

    public void asyncStringResponse() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/asyncStringResponse", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "test");
                });
                testComplete();
            }
        });
        request.end();
        await();

    }

    @Test
    public void asyncStringResponseParameter() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/asyncStringResponseParameter/123", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "123");
                });
                testComplete();
            }
        });
        request.end();
        await();

    }



    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(value = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {

        @Path("/asyncStringResponse")
        @GET
        public void rsAsyncStringResponse(RestHandler reply) throws InterruptedException {
            System.out.println("asyncStringResponse: " + reply);
            reply.response().async().stringResponse(() -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return "test";
            }).execute();
        }

        @Path("/asyncByteResponse")
        @GET
        public void rsAsyncByteResponse(RestHandler reply) throws InterruptedException {
            System.out.println("asyncStringResponse: " + reply);
            reply.response().async().byteResponse(() -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return "test".getBytes();
            }).execute();
        }

        @Path("/asyncObjectResponse")
        @GET
        public void rsAsyncObjectResponse(RestHandler reply) throws InterruptedException {
            System.out.println("asyncStringResponse: " + reply);
            reply.response().async().objectResponse(() -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return new Payload<String>("test");
            }, new ExampleStringEncoder()).execute();
        }


        @Path("/asyncStringResponseParameter/:help")
        @GET
        public void rsAsyncStringResponseParameter(RestHandler handler) {
            String productType = handler.request().param("help");
            System.out.println("asyncStringResponseParameter: " + handler);
            handler.response().async().stringResponse(() -> {
                System.out.println("WAIT");
                Thread.sleep(2500);
                System.out.println("WAIT END");
                return productType;
            }).execute();
        }


    }


}
