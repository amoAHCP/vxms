package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.OperationType;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.common.Type;
import org.jacpfx.entity.Payload;
import org.jacpfx.vertx.services.VertxServiceEndpoint;
import org.jacpfx.vertx.websocket.response.WSHandler;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class WSServiceSelfhostedTest extends VertxTestBase {
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

    public void simpleConnectAndWrite() throws InterruptedException {


        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/simpleConnectAndWrite", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("client data simpleConnectAndWrite:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.close();
                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time simpleConnectAndWrite: " + (endTime - startTime) + "ms");
                testComplete();
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    @Test
    public void simpleConnectAndAsyncWrite() throws InterruptedException {

        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/asyncReply", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("client data simpleConnectAndAsyncWrite:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.close();
                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time simpleConnectAndAsyncWrite: " + (endTime - startTime) + "ms");
                testComplete();
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    @Test
    public void simpleConnectOnTwoThreads() throws InterruptedException {
        ExecutorService s = Executors.newFixedThreadPool(2);
        CountDownLatch latchMain = new CountDownLatch(2);
        Runnable r = () -> {

            getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/simpleConnectAndWrite", ws -> {
                long startTime = System.currentTimeMillis();
                ws.handler((data) -> {
                    System.out.println("client data simpleConnectOnTwoThreads:" + new String(data.getBytes()));
                    assertNotNull(data.getString(0, data.length()));
                    ws.close();
                    latchMain.countDown();
                    long endTime = System.currentTimeMillis();
                    System.out.println("round trip time simpleConnectOnTwoThreads: " + (endTime - startTime) + "ms");
                });

                ws.writeFrame(new WebSocketFrameImpl("yhello"));
            });

        };

        s.submit(r);
        s.submit(r);

        latchMain.await();


    }

    @Test
    public void simpleReplyToOnTwoThreads() throws InterruptedException {
        ExecutorService s = Executors.newFixedThreadPool(2);
        CountDownLatch latchMain = new CountDownLatch(2);
        Runnable r = () -> {

            getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/simpleConnectAndWrite", ws -> {
                long startTime = System.currentTimeMillis();
                ws.handler((data) -> {
                    System.out.println("client data simpleConnectOnTwoThreads:" + new String(data.getBytes()));
                    assertNotNull(data.getString(0, data.length()));
                    ws.close();
                    latchMain.countDown();
                    long endTime = System.currentTimeMillis();
                    System.out.println("round trip time simpleConnectOnTwoThreads: " + (endTime - startTime) + "ms");
                });

                ws.writeFrame(new WebSocketFrameImpl("yhello"));
            });

        };

        s.submit(r);
        s.submit(r);

        latchMain.await();


    }

    @Test
    public void simpleConnectOnTenThreads() throws InterruptedException {
        int counter = 100;
        ExecutorService s = Executors.newFixedThreadPool(counter);
        CountDownLatch latchMain = new CountDownLatch(counter);


        for (int i = 0; i <= counter; i++) {
            Runnable r = () -> {

                getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/replyto1", ws -> {
                    long startTime = System.currentTimeMillis();
                    ws.handler((data) -> {
                        System.out.println("client data simpleConnectOnTenThreads:" + new String(data.getBytes()));
                        assertNotNull(data.getString(0, data.length()));
                        ws.close();
                        latchMain.countDown();
                        long endTime = System.currentTimeMillis();
                        System.out.println("round trip time simpleConnectOnTenThreads: " + (endTime - startTime) + "ms");
                    });

                   // ws.writeFrame(new WebSocketFrameImpl("zhello"));
                    ws.writeFinalTextFrame("zhello");
                });


            };

            s.submit(r);
        }


        latchMain.await();


    }

    @Test
    public void simpleMutilpeReply() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintTwo", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReply:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                if (counter.incrementAndGet() == MAX_RESPONSE_ELEMENTS) {
                    ws.close();
                    testComplete();
                }

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    @Test
    public void simpleByteReply() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/binaryReply", ws -> {

            ws.handler((data) -> {
                System.out.println("client data binaryReply:" );
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

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }



    @Test
    public void testGetObjectAndReplyObject() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/getObjectAndReplyObject", ws -> {

            ws.handler((data) -> {
                System.out.println("client data objectReply:" );
                assertNotNull(data.getBytes());
                try {
                    Payload<String> payload = (Payload<String>) Serializer.deserialize(data.getBytes());
                    System.out.println("value:::"+payload.getValue());
                    assertTrue(payload.equals(new Payload<String>("xhello")));
                    System.out.println(payload);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                ws.close();
                testComplete();

            });
            Payload<String> p = new  Payload<String>("xhello");
            try {
                ws.write(Buffer.buffer(Serializer.serialize(p)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        await();

    }


    @Test
    public void simpleObjectReply() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectReply", ws -> {

            ws.handler((data) -> {
                System.out.println("client data objectReply:" );
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

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();

    }

    @Test
    public void simpleMutilpeReplyToAll() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/mutilpeReplyToAll", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReplyToAll:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                if (counter.incrementAndGet() == MAX_RESPONSE_ELEMENTS) {
                    ws.close();
                    testComplete();
                }

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();
    }

    @Test
    public void simpleMutilpeReplyToAll_1() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintFour", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReplyToAll_1:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.close();
                testComplete();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await();
    }

    @Test
    public void simpleMutilpeReplyToAllThreaded() throws InterruptedException {
        ExecutorService s = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(2);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintFour", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReplyToAllThreaded:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                latch.countDown();
                ws.close();

            });


        });

        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintFour", ws -> {

            ws.handler((data) -> {
                System.out.println("client datasimpleMutilpeReplyToAllThreaded 5.1:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                latch.countDown();
                ws.close();


            });

            ws.writeFrame(new WebSocketFrameImpl("xhello simpleMutilpeReplyToAllThreaded"));

        });


        latch.await();
    }

    public HttpClient getClient() {
        return client;
    }


    @org.jacpfx.common.ServiceEndpoint(value = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VertxServiceEndpoint {
        @Path("/wsEndpintOne")
        @OperationType(Type.WEBSOCKET)
        public void wsEndpointOne(WSHandler reply) {

        }

        @Path("/wsEndpintTwo")
        @OperationType(Type.WEBSOCKET)
        public void wsEndpointTwo(WSHandler reply) {

            replyAsyncTwo(reply.payload().getString() + "-3", reply);
            replyAsyncTwo(reply.payload().getString() + "-4", reply);
            replyAsyncTwo(reply.payload().getString() + "-5", reply);
            replyAsyncTwo(reply.payload().getString() + "-6", reply);
            System.out.println("wsEndpointTwo-2: " + name + "   :::" + this);
        }



        @Path("/mutilpeReplyToAll")
        @OperationType(Type.WEBSOCKET)
        public void wsEndpointThreeReplyToAll(WSHandler reply) {
            replyToAllAsync(reply.payload().getString() + "-3", reply);
            replyToAllAsync(reply.payload().getString() + "-4", reply);
            replyToAllAsync(reply.payload().getString() + "-5", reply);
            replyToAllAsync(reply.payload().getString() + "-6", reply);

            System.out.println("wsEndpointThreeReplyToAll-2: " + reply.payload().getString() + "   :::" + this);
        }


        @Path("/wsEndpintFour")
        @OperationType(Type.WEBSOCKET)
        public void wsEndpointThreeReplyToAllTwo(WSHandler reply) {
            replyToAllAsync(reply.payload().getString() + "-3", reply);
            System.out.println("+++ wsEndpointThreeReplyToAllTwo-4: " + reply.payload().getString() + "   :::" + this);
        }

        @Path("/simpleConnectAndWrite")
        @OperationType(Type.WEBSOCKET)
        public void wsEndpointHello(WSHandler reply) {


            reply.
                    response().
                    toCaller().
                    stringResponse(() -> reply.payload().getString() + "-2").
                    execute();
            System.out.println("wsEndpointHello-1: " + name + "   :::" + this);
        }

        @Path("/replyto1")
        @OperationType(Type.WEBSOCKET)
        public void wsEndpointReplyTo(WSHandler reply) {


            reply.
                    response().
                    to(reply.endpoint()). // reply to yourself
                    stringResponse(() -> reply.payload().getString() + "-2").
                    execute();
            System.out.println("wsEndpointHello-1: " + name + "   :::" + this);
        }


        @Path("/asyncReply")
        @OperationType(Type.WEBSOCKET)
        // @Encoder
        // @Decoder
        public void wsEndpointAsyncReply(WSHandler reply) {

            reply.
                    response().
                    async().
                    toCaller().
                    stringResponse(() -> reply.payload().getString() + "-2").
                    execute();
            System.out.println("wsEndpointAsyncReply-1: " + name + "   :::" + this);
        }

        @Path("/binaryReply")
        @OperationType(Type.WEBSOCKET)
        // @Encoder
        // @Decoder
        public void wsEndpointBinaryReply(WSHandler reply) {

            reply.
                    response().
                    toCaller().
                    byteResponse(() -> {
                        try {
                            Payload<String> p = new Payload<String>(reply.payload().getString().get());
                            byte[] b= Serializer.serialize(p);
                            return b;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return new byte[0];
                    }).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @Path("/objectReply")
        @OperationType(Type.WEBSOCKET)
        // @Encoder
        // @Decoder
        public void wsEndpointObjectReply(WSHandler reply) {

            reply.
                    response().
                    toCaller().
                    objectResponse(()->new Payload<String>(reply.payload().getString().get()),new ExampleByteEncoder()).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @Path("/getObjectAndReplyObject")
        @OperationType(Type.WEBSOCKET)
        // @Encoder
        // @Decoder
        public void wsEndpointGetObjectAndReplyObject(WSHandler reply) {
            System.out.println("1:-----------");
            reply.payload().getObject(MyTestObject.class, new ExampleByteDecoderMyTest()).ifPresent(payload ->{
                System.out.println("should never be called");
            });
            System.out.println("2:-----------");
            reply.payload().getObject(Payload.class, new ExampleByteDecoderPayload()).ifPresent(payload ->{
                reply.
                        response().
                        toCaller().
                        objectResponse(()->new Payload<String>((String) payload.getValue()),new ExampleByteEncoder()).
                        execute();
            });

            System.out.println("3:-----------");


            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }


        private void replyAsyncTwo(String name, WSHandler reply) {
            reply.response().async().toCaller().stringResponse(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return name + Thread.currentThread();
            }).execute();
        }

        private void replyToAllAsync(String name, WSHandler reply) {
            reply.
                    response().
                    async().
                    toAll().
                    stringResponse(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return name + Thread.currentThread();
            }).execute();
        }
    }


}
