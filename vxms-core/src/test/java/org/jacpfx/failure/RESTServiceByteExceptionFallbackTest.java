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
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.vertx.rest.annotation.OnRestError;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andy Moncsek on 23.04.15.
 */

public class RESTServiceByteExceptionFallbackTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
    public static final int PORT = 9977;

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
        // don'failure have to hardecode it in your tests

        getVertx().deployVerticle(new WsServiceOne(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            System.out.println("start service: " + asyncResult.cause());
            assertTrue(asyncResult.succeeded());
            assertNotNull("deploymentID should not be null", asyncResult.result());
            // If deployed correctly then start the tests!
            //   latch2.countDown();

            latch2.countDown();

        });

        awaitLatch(latch2);


    }


    @Test
    public void exceptionInByteResponseWithErrorHandler() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/exceptionInByteResponseWithErrorHandler?val=123&tmp=456", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    String val = body.getString(0, body.length());
                    System.out.println("--------exceptionInStringResponse: " + val);
                    //assertEquals(key, "val");
                    testComplete();
                });


            }
        });
        request.end();

        await(5000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void exceptionInErrorHandler() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.
                createHttpClient(options);

        HttpClientRequest request = client.get("/wsService/exceptionInErrorHandler", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {
                    Payload<String> val = null;
                    try {
                        val = (Payload<String>) Serializer.deserialize(body.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    System.out.println("--------exceptionInStringResponse: " + val);
                    assertEquals(val.getValue(), "catched");
                    testComplete();
                });


            }
        });
        request.end();

        await(5000, TimeUnit.MILLISECONDS);

    }


    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {


        @Path("/exceptionInByteResponseWithErrorHandler")
        @GET
        public void rsexceptionInStringResponseWithErrorHandler(RestHandler handler) {
            handler.response().byteResponse((future) -> {
                throw new NullPointerException("Test");
                //return "";
            }).execute();
        }

        @OnRestError("/exceptionInByteResponseWithErrorHandler")
        @GET
        public void errorMethod1(RestHandler handler, Throwable t) {
            System.out.println("+++++++rsexceptionInStringResponseWithErrorHandlerError: " + handler);
            t.printStackTrace();
            System.out.println("----------------------------------");
            throw new NullPointerException("test...1234");
        }


        @Path("/exceptionInErrorHandler")
        @GET
        public void exceptionInErrorHandler(RestHandler handler) {
            System.out.println("exceptionInErrorHandler: " + handler);
            handler.response().byteResponse((future) -> {
                throw new NullPointerException("Test");
                //return "";
            }).onFailureRespond((error, response) -> {
                throw new NullPointerException("Test2");
            }).execute();
        }

        @OnRestError("/exceptionInErrorHandler")
        @GET
        public void exceptionInErrorHandlerErrorHandlerError(RestHandler handler, Throwable t) {
            System.out.println("+++++++exceptionInErrorHandler: " + t.getMessage());
            t.printStackTrace();
            System.out.println("----------------------------------");
            handler.response().byteResponse(h -> h.complete(Serializer.serialize( new Payload<>("catched")))).execute();
        }
    }
}
