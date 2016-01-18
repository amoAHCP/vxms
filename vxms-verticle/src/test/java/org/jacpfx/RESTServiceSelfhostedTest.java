package org.jacpfx;


import com.google.gson.Gson;
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
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.entity.encoder.ExampleStringEncoder;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTServiceSelfhostedTest extends VertxTestBase {
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

    public void endpointOne() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointOne", new Handler<HttpClientResponse>() {
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

    public void endpointTwo() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointTwo/123", new Handler<HttpClientResponse>() {
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

    @Test

    public void endpointThree() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointThree?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    assertEquals(body.toString(), "123456");
                });
                testComplete();
            }
        });
        request.end();
        await();

    }

    @Test

    public void endpointFourErrorRetryTest() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointFourErrorRetryTest?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    assertEquals(body.toString(), "123456");
                    testComplete();
                });

            }
        });
        request.end();
        await();

    }

    @Test
    public void endpointFourErrorReturnRetryTest() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointFourErrorReturnRetryTest?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response endpointFourErrorReturnRetryTest: " + body.toString());
                    assertEquals(body.toString(), "456123");
                    testComplete();
                });

            }
        });
        request.end();
        await();

    }

    @Test

    public void endpointFive() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointFive?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Payload<String> pp = new Gson().fromJson(body.toString(), Payload.class);
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());

                });
                testComplete();
            }
        });
        request.end();
        await();

    }


    @Test

    public void endpointFive_error() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointFive_error?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Payload<String> pp = new Gson().fromJson(body.toString(), Payload.class);
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());

                });
                testComplete();
            }
        });
        request.end();
        await();

    }

    @Test

    public void endpointSix() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointSix?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Payload<String> pp = null;
                    try {
                        pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());

                });
                testComplete();
            }
        });
        request.end();
        await();

    }

    @Test

    public void endpointSeven() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointSeven?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Payload<String> pp = null;
                    try {
                        pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());

                });
                testComplete();
            }
        });
        request.end();
        await();

    }

    @Test

    public void endpointSeven_error() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/endpointSeven_error?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    System.out.println("Got a response: " + body.toString());
                    Payload<String> pp = null;
                    try {
                        pp = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    assertEquals(pp.getValue(), new Payload<>("123" + "456").getValue());

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

        @Path("/endpointOne")
        @GET
        public void rsEndpointOne(RestHandler reply) {
            System.out.println("wsEndpointOne: " + reply);
            reply.response().stringResponse(() -> "test").execute();
        }

        @Path("/endpointTwo/:help")
        @GET
        public void rsEndpointTwo(RestHandler handler) {
            String productType = handler.request().param("help");
            System.out.println("wsEndpointTwo: " + handler);
            handler.response().stringResponse(() -> productType).execute();
        }

        @Path("/endpointThree")
        @GET
        public void rsEndpointThree(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            handler.response().stringResponse(() -> productType + product).execute();
        }

        @Path("/endpointFourErrorRetryTest")
        @GET
        public void rsEndpointFourErrorRetryTest(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            AtomicInteger count = new AtomicInteger(4);
            handler.response().stringResponse(() -> {
                if (count.decrementAndGet() >= 0) {
                    System.out.println("throw:" + count.get());
                    throw new NullPointerException("test");
                }
                return productType + product;
            }).onError(error -> {
                error.printStackTrace();
                System.out.println("count: " + count.get());
                if (count.get() == 1) {
                    handler.response().stringResponse(() -> {
                        return productType + product;
                    }).execute();
                }

            }).retry(3).execute();
        }

        @Path("/endpointFourErrorReturnRetryTest")
        @GET
        public void rsEndpointFourErrorReturnRetryTest(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            AtomicInteger count = new AtomicInteger(4);
            handler.response().stringResponse(() -> {
                if (count.decrementAndGet() >= 0) {
                    System.out.println("throw:" + count.get());
                    throw new NullPointerException("test");
                }
                return productType + product;
            }).onError(error -> System.out.println("retry: " + count.get() + "   " + error.getStackTrace())).retry(3).onStringResponseError(error -> product + productType).execute();
        }

        @Path("/endpointFive")
        @GET
        public void rsEndpointFive(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            Payload<String> pp = new Payload<>(productType + product);
            handler.response().objectResponse(() -> pp, new ExampleStringEncoder()).execute();
        }

        @Path("/endpointFive_error")
        @GET
        public void rsEndpointFive_error(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            Payload<String> pp = new Payload<>(productType + product);
            AtomicInteger count = new AtomicInteger(4);
            handler.response().objectResponse(() -> {
                        if (count.decrementAndGet() >= 0) {
                            System.out.println("throw:"+count.get());
                            throw new NullPointerException("test");
                        }
                        return new Payload<>("hallo");
                    }
                    , new ExampleStringEncoder()
            ).retry(3).onObjectResponseError(error-> pp, new ExampleStringEncoder()).execute();
        }

        @Path("/endpointSix")
        @GET
        public void rsEndpointSix(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            Payload<String> pp = new Payload<>(productType + product);
            handler.response().objectResponse(() -> pp, new ExampleByteEncoder()).execute();
        }

        @Path("/endpointSeven")
        @GET
        public void rsEndpointSeven(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            Payload<String> pp = new Payload<>(productType + product);
            handler.response().byteResponse(() -> Serializer.serialize(pp)).execute();
        }

        @Path("/endpointSeven_error")
        @GET
        public void rsEndpointSeven_error(RestHandler handler) {
            String productType = handler.request().param("val");
            String product = handler.request().param("tmp");
            System.out.println("wsEndpointTwo: " + handler);
            AtomicInteger count = new AtomicInteger(4);
            Payload<String> pp = new Payload<>(productType + product);
            handler.response().byteResponse(() -> {
                if (count.decrementAndGet() >= 0) {
                    System.out.println("throw:"+count.get());
                    throw new NullPointerException("test");
                }
                return Serializer.serialize(pp);
            }).retry(3).onByteResponseError(error -> {
                try {
                    return Serializer.serialize(pp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).execute();
        }


    }


}
