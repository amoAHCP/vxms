package org.jacpfx;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketError;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class WSServiceExceptionTests extends VertxTestBase {
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
    public void exceptionTests01() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/exceptionTests01", ws -> {

            ws.handler((data) -> {
                assertNotNull(data.getString(0, data.length()));

                String payload = data.getString(0, data.length());
                //assertTrue(payload.equals("xhello"));
                System.out.println(payload);

                ws.close();
                testComplete();
            });
            ws.closeHandler(handler -> {
                System.out.println("CLOSED");
                testComplete();
            });
            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    @Test
    public void exceptionTestsAsync01() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/exceptionTestsAsync01", ws -> {

            ws.handler((data) -> {
                assertNotNull(data.getString(0, data.length()));

                String payload = data.getString(0, data.length());
                //assertTrue(payload.equals("xhello"));
                System.out.println(payload);

                ws.close();
                testComplete();
            });
            ws.closeHandler(handler -> {
                System.out.println("CLOSED");
                testComplete();
            });
            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    @Test
    public void exceptionTests02() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/exceptionTests02", ws -> {

            ws.handler((data) -> {
                assertNotNull(data.getString(0, data.length()));

                String payload = data.getString(0, data.length());
                //assertTrue(payload.equals("xhello"));
                System.out.println(payload);

                ws.close();
                testComplete();
            });
            ws.closeHandler(handler -> {
                System.out.println("CLOSED");
                testComplete();
            });
            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(value = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {



        @OnWebSocketMessage("/exceptionTests01")
        public void wsEndpointExceptionTests01(WebSocketHandler reply) {
            reply.
                    response().
                    reply().
                    stringResponse(() -> {
                        System.out.println("Exception");
                        throw new NullPointerException("test");
                    }).
                    execute();
            System.out.println("Exception END");
        }

        @OnWebSocketError("/exceptionTests01---")
        public void wsEndpointExceptionTests01Error(WebSocketHandler reply,Throwable t) {
            t.printStackTrace();
            System.out.println("----failover");
        }

        @OnWebSocketMessage("/exceptionTestsAsync01")
        public void wsEndpointExceptionTestsAsync01(WebSocketHandler reply) {
            reply.
                    response().async().
                    reply().
                    stringResponse(() -> {
                        System.out.println("Exception");
                        throw new NullPointerException("test");
                    }).
                    execute();
            System.out.println("Exception END");
        }


        @OnWebSocketError("/exceptionTestsAsync01---")
        public void wsEndpointExceptionTestsAsync01Error(WebSocketHandler reply,Throwable t) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.response().reply().stringResponse(()->"").execute();
        }


        @OnWebSocketMessage("/exceptionTests02")
        public void wsEndpointExceptionTests02(WebSocketHandler reply) {
            System.out.println("Exception --XXXX");
            throw new NullPointerException("test");
        }


        @OnWebSocketError("/exceptionTests02---")
        public void wsEndpointExceptionTests02Error(WebSocketHandler reply,Throwable t) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.response().reply().stringResponse(()->"").execute();
        }


    }





    private void handleInSimpleTests(WebSocket ws, Buffer data) {
        System.out.println("client data simpleRetry:");
        assertNotNull(data.getBytes());
        try {
            Payload<String> payload = (Payload<String>) Serializer.deserialize(data.getBytes());
            assertTrue(payload.equals(new Payload<String>("xhello")));
            System.out.println(payload);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ws.close();
        testComplete();
    }

    private void handleInSimpleStringTests(WebSocket ws, Buffer data) {
        System.out.println("client data simpleRetry:");
        assertNotNull(data.getBytes());
        String payload = data.getString(0,data.length());
        assertTrue(payload.equals("xhello"));
        System.out.println(payload);
        ws.close();
        testComplete();
    }
}
