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
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientEventByteResponseTest extends VertxTestBase {
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


        CountDownLatch latch2 = new CountDownLatch(3);
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

    public void complexByteResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/complexByteResponse");
        Future<byte[]> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<byte[]>() {

            @Override
            public void completed(byte[] response) {
                Payload<String> pp = null;
                try {
                    pp = (Payload<String>) Serializer.deserialize(response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String value = pp.getValue();
                vertx.runOnContext( h -> {

                    assertEquals(value, new Payload<>("hello").getValue());
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
    public void complexByteErrorResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/complexByteErrorResponse");
        Future<byte[]> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<byte[]>() {

            @Override
            public void completed(byte[] response) {
                System.out.println("Response entity '" + response + "' received.");
                Payload<String> pp = null;
                try {
                    pp = (Payload<String>) Serializer.deserialize(response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String value = pp.getValue();
                vertx.runOnContext( h -> {

                    assertEquals(value, new Payload<>("test exception").getValue());
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
    public void simpleByteNoConnectionErrorResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/simpleByteNoConnectionErrorResponse");
        Future<byte[]> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<byte[]>() {

            @Override
            public void completed(byte[] response) {
                System.out.println("Response entity '" + response + "' received.");
                Payload<String> pp = null;
                try {
                    pp = (Payload<String>) Serializer.deserialize(response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String value = pp.getValue();
                vertx.runOnContext( h -> {

                    assertEquals(value, new Payload<>("no connection").getValue());
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
    public void simpleByteNoConnectionRetryErrorResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/simpleByteNoConnectionRetryErrorResponse");
        Future<byte[]> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<byte[]>() {

            @Override
            public void completed(byte[] response) {
                System.out.println("Response entity '" + response + "' received.");
                Payload<String> pp = null;
                try {
                    pp = (Payload<String>) Serializer.deserialize(response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                String value = pp.getValue();
                vertx.runOnContext( h -> {

                    assertEquals(value, new Payload<>("hello1").getValue());
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
    public void simpleByteNoConnectionExceptionRetryErrorResponseTest() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/simpleByteNoConnectionExceptionRetryErrorResponse");
        Future<byte[]> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<byte[]>() {

            @Override
            public void completed(byte[] response) {
                System.out.println("Response entity '" + response + "' received.");
                Payload<String> pp = null;
                try {
                    pp = (Payload<String>) Serializer.deserialize(response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String value = pp.getValue();
                vertx.runOnContext( h -> {

                    assertEquals(value, new Payload<>("hello1").getValue());
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


    @ServiceEndpoint(name = SERVICE_REST_GET, port = PORT2)
    public class WsServiceTwo extends VxmsEndpoint {


        @Path("/complexByteResponse")
        @GET
        public void complexByteResponse(RestHandler reply) {
            System.out.println("CALL");
            reply.
                    eventBusRequest().
                    send("hello", "welt").
                    mapToByteResponse((handler, replyFuture)  -> {
                        Payload<String> p = new Payload<>(handler.
                                result().
                                body().toString());
                        replyFuture.complete(Serializer.serialize(p));
                    }).
                    execute();
        }


        @Path("/complexByteErrorResponse")
        @GET
        public void complexByteErrorResponse(RestHandler reply) {
            reply.eventBusRequest().
                    send("hello", "welt").
                    mapToByteResponse((handler, replyFuture)  -> {
                        throw new NullPointerException("test exception");
                    }).
                    onFailureRespond((error, future) -> {
                        try {
                            Payload<String> p = new Payload<>(error.getMessage());
                            future.complete(Serializer.serialize(p));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        future.complete(new byte[0]);
                    }).
                    execute();
        }

        @Path("/simpleByteNoConnectionErrorResponse")
        @GET
        public void simpleByteNoConnectionErrorResponse(RestHandler reply) {
            reply.eventBusRequest().
                    send("hello1", "welt").
                    mapToByteResponse((handler, replyFuture) -> replyFuture.complete((byte[]) handler.result().body())).
                    onFailureRespond((t, c) -> {
                        try {
                            Payload<String> p = new Payload<>("no connection");
                            c.complete(Serializer.serialize(p));
                        } catch (IOException e) {
                            c.fail(e);
                        }
                    }).
                    execute();
        }

        @Path("/simpleByteNoConnectionRetryErrorResponse")
        @GET
        public void simpleByteNoConnectionRetryErrorResponse(RestHandler reply) {
            reply.eventBusRequest().
                    send("error", "1").
                    mapToByteResponse((handler, replyFuture) -> {
                        Payload<String> p = new Payload<>(handler.result().body().toString() + "1");
                        replyFuture.complete(Serializer.serialize(p));
                    }).
                    retry(3).
                    execute();
        }

        @Path("/simpleByteNoConnectionExceptionRetryErrorResponse")
        @GET
        public void simpleByteNoConnectionExceptionRetryErrorResponse(RestHandler reply) {
            AtomicInteger count = new AtomicInteger(0);
            reply.eventBusRequest().
                    send("hello", "welt").
                    mapToByteResponse((handler, replyFuture) -> {
                        System.out.println("retry: " + count.get());
                        if (count.incrementAndGet() < 3) throw new NullPointerException("test");
                        Payload<String> p = new Payload<>(handler.result().body().toString() + "1");
                        replyFuture.complete(Serializer.serialize(p));
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
