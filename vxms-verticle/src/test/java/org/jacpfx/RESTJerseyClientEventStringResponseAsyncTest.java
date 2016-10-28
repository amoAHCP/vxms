package org.jacpfx;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientEventStringResponseAsyncTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
    public static final int PORT = 9998;
    public static final int PORT2 = 9999;
    public static final int PORT3 = 9991;

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


        CountDownLatch latch2 = new CountDownLatch(2);
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
        // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
        // don't have to hardecode it in your tests


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


        client = getVertx().
                createHttpClient(new HttpClientOptions());
        awaitLatch(latch2);

    }


    @Test

    public void simpleResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/simpleResponse");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response, "hello");
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

    public void complexResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" + PORT2).path("/wsService/complexResponse");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response, "hello");
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

    public void complexErrorResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" + PORT2).path("/wsService/complexErrorResponse");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response, "test exception");
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

    public void onErrorResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" + PORT2).path("/wsService/onFailurePass");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                vertx.runOnContext(c -> {
                    assertEquals(response, "failed");
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
    public void onErrorResponseErrorTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" + PORT2).path("/wsService/onFailurePassError");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                vertx.runOnContext(c -> {
                    assertEquals(response, "failed");
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


        @Path("/simpleResponse")
        @GET
        public void simpleResponse(RestHandler reply) {
            reply.eventBusRequest().sendAndRespondRequest("hello", "welt");
        }

        @Path("/complexResponse")
        @GET
        public void complexResponse(RestHandler reply) {
            reply.
                    eventBusRequest().async().
                    send("hello", "welt").
                    mapToStringResponse(handler ->
                            handler.
                                    result().
                                    body().toString()).
                    execute();
        }


        @Path("/complexErrorResponse")
        @GET
        public void complexErrorResponse(RestHandler reply) {
            reply.eventBusRequest().async().
                    send("hello", "welt").
                    mapToStringResponse(handler -> {
                        throw new NullPointerException("test exception");
                    }).
                    onFailureRespond(error -> error.getMessage()).
                    execute();
        }


        @Path("/complexChainErrorResponse")
        @GET
        public void complexChainErrorResponse(RestHandler reply) {
            reply.eventBusRequest().async().
                    send("hello", "welt").
                    mapToStringResponse(handler -> {
                        throw new NullPointerException("test exception");
                    }).
                    retry(3).
                    onFailureRespond(error -> error.getMessage()).
                    execute();
        }

        @Path("/onFailurePass")
        @GET
        public void onErrorResponse(RestHandler reply) {
            reply.
                    eventBusRequest().
                    async().
                    send("hello1", "welt").
                    mapToStringResponse(handler -> handler.result().body().toString()).
                    onFailureRespond(handler -> {
                        System.err.println("::: " + handler.getMessage());
                        return "failed";
                    }).execute();
        }

        @Path("/onFailurePassError")
        @GET
        public void onFailurePassError(RestHandler reply) {
            reply.
                    eventBusRequest().
                    async().
                    send("hello1", "welt").
                    mapToStringResponse(handler -> handler.result().body().toString()).
                    onFailureRespond(handler -> {
                        System.err.println("::: " + handler.getCause().getMessage());
                        return "failed";
                    }).execute();
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


}
