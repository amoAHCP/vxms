package org.jacpfx;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class WSServiceTimerTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
    public static final int PORT = 9998;

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

        getVertx().deployVerticle(new WsServiceOne(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            assertTrue(asyncResult.succeeded());
            assertNotNull("deploymentID should not be null", asyncResult.result());
            // If deployed correctly then start the tests!
            //   latch2.countDown();

            latch2.countDown();

        });

        getVertx().deployVerticle(new ProducerVerticle(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            assertTrue(asyncResult.succeeded());
            assertNotNull("deploymentID should not be null", asyncResult.result());
            // If deployed correctly then start the tests!
            //   latch2.countDown();

            latch2.countDown();

        });



        awaitLatch(latch2);

    }


    @Test

    public void wsPeriodicTest() throws InterruptedException {


        final HttpClient client = getClient();
        client.websocket(PORT, HOST, SERVICE_REST_GET + "/wsPeriodic", ws -> {
            final AtomicInteger count = new AtomicInteger(0);
            ws.handler((data) -> {
                System.out.println("client data wsPeriodicTest:" + new String(data.getBytes()));
                String value =data.getString(0, data.length());
                assertNotNull(value);
                 assertTrue(value.equals("xhello-"+count.incrementAndGet()));
                if(count.get() ==10) {
                    ws.close();
                    testComplete();
                }

               // ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(20000, TimeUnit.MILLISECONDS);
        client.close();
    }

    @Test

    public void wsPassThroughTest() throws InterruptedException {


        final HttpClient client = getClient();
        client.websocket(PORT, HOST, SERVICE_REST_GET + "/wsPassThrough", ws -> {
            final AtomicInteger count = new AtomicInteger(0);
            ws.handler((data) -> {
                System.out.println("client data wsPassThroughTest:" + new String(data.getBytes()));
                String value =data.getString(0, data.length());
                assertNotNull(value);
                assertTrue(value.equals("xhello-"+count.incrementAndGet()));
                if(count.get() ==10) {
                    ws.close();
                    testComplete();
                }

                // ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(20000, TimeUnit.MILLISECONDS);
        client.close();
    }



    public HttpClient getClient() {
        return  getVertx().
                createHttpClient(new HttpClientOptions());
    }


    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {


        @OnWebSocketMessage("/wsPeriodic")
        public void wsPeriodic(WebSocketHandler reply) {
            final AtomicInteger count = new AtomicInteger(0);
            vertx.setPeriodic(100, id -> {
                reply.response().reply(). stringResponse(() -> reply.payload().getString().get() + "-"+count.incrementAndGet()).
                        execute();
            });

        }

        @OnWebSocketMessage("/wsPassThrough")
        public void wsPassThrough(WebSocketHandler reply) {
            vertx.eventBus().consumer("consumer", consumer -> {
                reply.response().reply(). stringResponse(() -> reply.payload().getString().get() + "-"+consumer.body()).
                        execute();
            });
            vertx.eventBus().send("producer","");
        }



    }

    public class ProducerVerticle extends AbstractVerticle {

        public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
            vertx.eventBus().consumer("producer", handler -> {
                AtomicLong counter = new AtomicLong(0L);
                vertx.setPeriodic(100, id -> {
                    vertx.eventBus().send("consumer",counter.incrementAndGet()+"");
                });

            });
            startFuture.complete();
        }
    }


}
