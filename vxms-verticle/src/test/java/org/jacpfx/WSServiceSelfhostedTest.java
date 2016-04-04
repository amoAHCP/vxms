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
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.entity.decoder.ExampleByteDecoderMyTest;
import org.jacpfx.entity.decoder.ExampleByteDecoderPayload;
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketClose;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketError;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketOpen;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;
import org.junit.Before;
import org.junit.Test;

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



        awaitLatch(latch2);

    }


    @Test

    public void simpleConnectAndWrite() throws InterruptedException {


        final HttpClient client = getClient();
        client.websocket(PORT, HOST, SERVICE_REST_GET + "/simpleConnectAndWrite", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("client data simpleConnectAndWrite:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));

                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time simpleConnectAndWrite: " + (endTime - startTime) + "ms");
                ws.closeHandler((x) ->{
                            System.out.println("close simpleConnectAndWrite");
                            testComplete();
                        }
                );

                ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);
        client.close();
    }

    @Test
    public void simpleConnectAndAsyncWrite() throws InterruptedException {

        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/asyncReply", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("client data simpleConnectAndAsyncWrite:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));

                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time simpleConnectAndAsyncWrite: " + (endTime - startTime) + "ms");
                ws.closeHandler((x) ->{
                            System.out.println("close simpleConnectAndAsyncWrite");
                            testComplete();
                        }
                );

                ws.close();
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });

        System.out.println("await");
        await();
        System.out.println("finish");

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
        testComplete();


    }

    @Test
    //@Ignore
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
        testComplete();


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

testComplete();
    }

    @Test
    public void simpleMutilpeReply() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintTwo", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReply:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                if (counter.incrementAndGet() == MAX_RESPONSE_ELEMENTS) {
                    ws.closeHandler((x) ->{
                                System.out.println("close simpleMutilpeReply");
                                testComplete();
                            }
                    );

                    ws.close();

                }

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void simpleByteReply() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/binaryReply", ws -> {

            ws.handler((data) -> {
                System.out.println("client data binaryReply:");
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
                ws.closeHandler((x) ->{
                            System.out.println("close simpleByteReply");
                            testComplete();
                        }
                );

                ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void testGetObjectAndReplyObject() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/getObjectAndReplyObject", ws -> {

            ws.handler((data) -> {
                System.out.println("client data objectReply:");
                assertNotNull(data.getBytes());
                try {
                    Payload<String> payload = (Payload<String>) Serializer.deserialize(data.getBytes());
                    System.out.println("value:::" + payload.getValue());
                    assertTrue(payload.equals(new Payload<String>("xhello")));
                    System.out.println(payload);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println("close and compleate");
               //
                ws.closeHandler((x) ->{
                            System.out.println("close");
                            testComplete();
                }
                );

                ws.close();


            });
            Payload<String> p = new Payload<String>("xhello");
            try {
                ws.write(Buffer.buffer(Serializer.serialize(p)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void simpleObjectReply() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectReply", ws -> {

            ws.handler((data) -> {
                System.out.println("client data objectReply:");
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
                ws.closeHandler((x) ->{
                            System.out.println("close simpleObjectReply");
                            testComplete();
                        }
                );

                ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void simpleObjectReplyWithTimeout() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectReplyWithTimeout", ws -> {

            ws.handler((data) -> {
                System.out.println("client data objectReplyWithTimeout:");
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
                ws.closeHandler((x) ->{
                            System.out.println("close simpleObjectReplyWithTimeout");
                            testComplete();
                        }
                );

                ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void simpleMutilpeReplyToAll() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/mutilpeReplyToAll", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReplyToAll:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                if (counter.incrementAndGet() == MAX_RESPONSE_ELEMENTS) {
                    ws.closeHandler((x) ->{
                                System.out.println("close simpleMutilpeReplyToAll");
                                testComplete();
                            }
                    );

                    ws.close();
                }

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void simpleMutilpeReplyToAll_1() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintFour", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReplyToAll_1:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.closeHandler((x) ->{
                            System.out.println("close simpleMutilpeReplyToAll_1");
                            testComplete();
                        }
                );

                ws.close();

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void simpleMutilpeReplyToreplytoAllBut() throws InterruptedException {
        ExecutorService s = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(2);
        final CountDownLatch initLatch = new CountDownLatch(1);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/replytoAllBut", ws -> {




            getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/replytoAllBut", ws2 -> {

                ws.handler((data) -> {
                    System.out.println("client data simpleMutilpeReplyToreplytoAllBut 1:" + new String(data.getBytes()));
                    assertNotNull(data.getString(0, data.length()));
                    assertTrue(new String(data.getBytes()).equals("2"));
                    System.out.println("close ws: "+ new String(data.getBytes()));
                    ws.closeHandler((x) ->{
                                System.out.println("close simpleMutilpeReplyToreplytoAllBut");
                        latch.countDown();
                            }
                    );

                    ws.close();

                });

                ws2.handler((data) -> {
                    System.out.println("client simpleMutilpeReplyToreplytoAllBut 1.1:" + new String(data.getBytes()));
                    assertNotNull(data.getString(0, data.length()));

                    assertTrue(new String(data.getBytes()).equals("1"));
                    System.out.println("close ws2: "+ new String(data.getBytes()));
                    ws2.closeHandler((x) ->{
                                System.out.println("close simpleMutilpeReplyToreplytoAllBut 2");
                                latch.countDown();
                            }
                    );

                    ws2.close();


                });

                ws2.writeFrame(new WebSocketFrameImpl("2"));
                ws.writeFrame(new WebSocketFrameImpl("1"));

            });


        });



        latch.await();
    }

    @Test
    public void simpleMutilpeReplyToAllThreaded() throws InterruptedException {
        ExecutorService s = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(2);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintFour", ws -> {

            ws.handler((data) -> {
                System.out.println("client data simpleMutilpeReplyToAllThreaded:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.closeHandler((x) ->{
                            System.out.println("close datasimpleMutilpeReplyToAllThreaded");
                            latch.countDown();
                        }
                );

                ws.close();

            });


        });

        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/wsEndpintFour", ws -> {

            ws.handler((data) -> {
                System.out.println("client datasimpleMutilpeReplyToAllThreaded 5.1:" + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.closeHandler((x) ->{
                            System.out.println("close datasimpleMutilpeReplyToAllThreaded");
                            latch.countDown();
                        }
                );

                ws.close();


            });

            ws.writeFrame(new WebSocketFrameImpl("xhello simpleMutilpeReplyToAllThreaded"));

        });


        latch.await();
        testComplete();
    }

    public HttpClient getClient() {
        return  getVertx().
                createHttpClient(new HttpClientOptions());
    }


    @ServiceEndpoint(name = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {
        @OnWebSocketMessage("/wsEndpintOne")
        public void wsEndpointOne(WebSocketHandler reply) {

        }

        @OnWebSocketMessage("/wsEndpintTwo")
        public void wsEndpointTwo(WebSocketHandler reply) {

            replyAsyncTwo(reply.payload().getString() + "-3", reply);
            replyAsyncTwo(reply.payload().getString() + "-4", reply);
            replyAsyncTwo(reply.payload().getString() + "-5", reply);
            replyAsyncTwo(reply.payload().getString() + "-6", reply);
            System.out.println("wsEndpointTwo-2: " + name + "   :::" + this);
        }


        @OnWebSocketMessage("/mutilpeReplyToAll")
        public void wsEndpointThreeReplyToAll(WebSocketHandler reply) {
            replyToAllAsync(reply.payload().getString() + "-3", reply);
            replyToAllAsync(reply.payload().getString() + "-4", reply);
            replyToAllAsync(reply.payload().getString() + "-5", reply);
            replyToAllAsync(reply.payload().getString() + "-6", reply);

            System.out.println("wsEndpointThreeReplyToAll-2: " + reply.payload().getString().get() + "   :::" + this);
        }


        @OnWebSocketMessage("/wsEndpintFour")
        public void wsEndpointThreeReplyToAllTwo(WebSocketHandler reply) {
            replyToAllAsync(reply.payload().getString().get() + "-3", reply);
            System.out.println("+++ wsEndpointThreeReplyToAllTwo-4: " + reply.payload().getString() + "   :::" + this);
        }

        @OnWebSocketMessage("/simpleConnectAndWrite")
        public void wsEndpointHello(WebSocketHandler reply) {


            reply.
                    response().
                    reply().
                    stringResponse(() -> reply.payload().getString().get() + "-2").
                    execute();
            System.out.println("wsEndpointHello-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/replyto1")
        public void wsEndpointReplyTo(WebSocketHandler reply) {


            reply.
                    response().
                    to(reply.endpoint()). // reply to yourself
                    stringResponse(() -> reply.payload().getString().get() + "-2").
                    execute();
            System.out.println("wsEndpointHello-1: " + name + "   :::" + this);
        }


        @OnWebSocketMessage("/asyncReply")
        public void wsEndpointAsyncReply(WebSocketHandler reply) {

            reply.
                    response().
                    async().
                    reply().
                    stringResponse(() -> reply.payload().getString() + "-2").
                    execute();
            System.out.println("wsEndpointAsyncReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/binaryReply")
        public void wsEndpointBinaryReply(WebSocketHandler reply) {

            reply.
                    response().
                    reply().
                    byteResponse(() -> {
                        try {
                            Payload<String> p = new Payload<String>(reply.payload().getString().get());
                            return Serializer.serialize(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return new byte[0];
                    }).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/objectReply")
        public void wsEndpointObjectReply(WebSocketHandler reply) {

            reply.
                    response().
                    reply().
                    objectResponse(() -> new Payload<String>(reply.payload().getString().get()), new ExampleByteEncoder()).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/objectReplyWithTimeout")
        public void wsEndpointObjectReplyWithTimeout(WebSocketHandler reply) {

            reply.
                    response().
                    async().
                    reply().
                    objectResponse(() -> new Payload<String>(reply.payload().getString().get()), new ExampleByteEncoder()).
                    timeout(2000).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/replytoAllBut")
        public void wsEndpointReplyToAllBut(WebSocketHandler reply) {
            System.out.println("replytoAllBut------: "+reply.payload().getString().get());

            reply.
                    response().
                    toAllBut(reply.endpoint()). // reply to other connected sessions
                    stringResponse(() -> reply.payload().getString().get()).
                    execute();
            System.out.println("-------------EXECUTE-------------------------");
            System.out.println("replytoAllBut: " + reply.endpoint() + "   :::" + reply.payload().getString().get());
        }

        @OnWebSocketOpen("/getObjectAndReplyObject")
        public void wsEndpointGetObjectAndReplyObjectOnOpen(WebSocketEndpoint endpoint) {
            System.out.println("OnOpen Endpoint: " + endpoint);
        }

        @OnWebSocketClose("/getObjectAndReplyObject")
        public void wsEndpointGetObjectAndReplyObjectOnClose(WebSocketEndpoint endpoint) {
            System.out.println("OnClose Endpoint: " + endpoint);
        }

        @OnWebSocketError("/getObjectAndReplyObject")
        public void wsEndpointGetObjectAndReplyObjectOnError(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            System.out.println("OnError Endpoint: " + endpoint + " :::" + t.getLocalizedMessage());
        }

        @OnWebSocketMessage("/getObjectAndReplyObject")
        public void wsEndpointGetObjectAndReplyObject(WebSocketHandler reply) {
            //  throw new NullPointerException("dfsdfs");
            System.out.println("1:-----------");
            reply.payload().getObject(new ExampleByteDecoderMyTest()).ifPresent(payload -> {
                System.out.println("should never be called");
            });
            System.out.println("2:-----------");
            reply.payload().getObject(new ExampleByteDecoderPayload()).ifPresent(payload -> {
                reply.
                        response().
                        reply().
                        objectResponse(() -> new Payload<String>((String) payload.getValue()), new ExampleByteEncoder()).
                        execute();
            });

            System.out.println("3:-----------");


            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }


        private void replyAsyncTwo(String name, WebSocketHandler reply) {
            reply.response().async().reply().stringResponse(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return name + Thread.currentThread();
            }).execute();
        }

        private void replyToAllAsync(String name, WebSocketHandler reply) {
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
