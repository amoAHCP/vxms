package org.jacpfx.websocket;


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
import org.jacpfx.entity.encoder.ExampleByteEncoder;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketError;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class WSServiceErrorAndRetry extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
    public static final int PORT = 9999;

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
    public void objectSimpleRetry() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectSimpleRetry", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void stringSimpleRetry() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/stringSimpleRetry", ws -> {

            ws.handler((data) -> {
                handleInSimpleStringTests(ws, data);

            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void objectUncatchedError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectUncatchedError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void stringUncatchedError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/stringUncatchedError", ws -> {

            ws.handler((data) -> {
                handleInSimpleStringTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void objectCatchedObjectError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectCatchedObjectError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void objectUnCatchedObjectError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectUnCatchedObjectError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void objectSimpleOnError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/objectSimpleOnError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void uncatchedErrorAsync() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/uncatchedErrorAsync", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void uncatchedTimeoutErrorAsync() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/uncatchedTimeoutErrorAsync", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(20000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void uncatchedTimeoutErrorException() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/uncatchedTimeoutErrorException", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void catchedTimeoutErrorException() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/catchedTimeoutErrorException", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void uncatchedMethodError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/uncatchedMethodError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void catchedError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/catchedError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void catchedErrorAsync() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/catchedErrorAsync", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void catchedByteError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/catchedByteError", ws -> {

            ws.handler((data) -> {
                handleInSimpleTests(ws, data);
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void catchedStringError() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/catchedStringError", ws -> {

            ws.handler((data) -> {
                assertNotNull(data.getString(0, data.length()));

                String payload = data.getString(0, data.length());
                assertTrue(payload.equals("xhello"));
                System.out.println(payload);

                ws.closeHandler((x) -> testComplete());
                ws.close();
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }


    @Test
    public void catchedAsyncStringErrorDelay() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        getClient().websocket(PORT, HOST, SERVICE_REST_GET + "/catchedAsyncStringErrorDelay", ws -> {

            ws.handler((data) -> {
                assertNotNull(data.getString(0, data.length()));

                String payload = data.getString(0, data.length());
                assertTrue(payload.equals("xhello"));
                System.out.println(payload);

                ws.closeHandler((x) -> testComplete());
                ws.close();
            });

            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

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

            });
            ws.closeHandler(handler -> {
                System.out.println("CLOSED");
                testComplete();
            });
            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

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

            });
            ws.closeHandler(handler -> {
                System.out.println("CLOSED");
                testComplete();
            });
            ws.writeFrame(new WebSocketFrameImpl("xhello"));
        });


        await(10000, TimeUnit.MILLISECONDS);

    }

    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {

        ////----------------- sync objectReply ------------------------------

        @OnWebSocketMessage("/objectSimpleRetry")
        public void wsEndpointObjectSimpleRetry(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(3);  //objectResponse(null).encoder(new Encoder)
            reply.
                    response().
                    reply().
                    objectResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }
                                System.out.println("return payload after 3 retries");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).retry(3).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/objectUncatchedError")
        public void wsEndpointObjectUncatchedError(WebSocketHandler reply) {
            reply.
                    response().
                    reply().
                    objectResponse(() -> {
                                System.out.println("throw");
                                throw new NullPointerException("test");
                            }, new ExampleByteEncoder()
                    ).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketError("/objectUncatchedError")
        public void wsEndpointObjectUncatchedErrorOnError(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).retry(3).
                    execute();
        }

        @OnWebSocketMessage("/objectCatchedObjectError")
        public void wsEndpointObjectCatchedObjectError(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    reply().
                    objectResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }

                                return null;
                            }, new ExampleByteEncoder()
                    ).
                    retry(3).
                    onFailureRespond((t) -> {
                        t.printStackTrace();
                        return new Payload<String>(reply.payload().getString().get());
                    }, new ExampleByteEncoder()).
                    execute();
        }

        @OnWebSocketMessage("/objectUnCatchedObjectError")
        public void wsEndpointObjectUnCatchedObjectError(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    reply().
                    objectResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }

                                return null;
                            }, new ExampleByteEncoder()
                    ).
                    retry(3).
                    onFailureRespond((t) -> {
                        throw new NullPointerException("test");
                    }, new ExampleByteEncoder()).
                    execute();
        }

        @OnWebSocketError("/objectUnCatchedObjectError")
        public void wsEndpointObjectUnCatchedObjectErrorOnError(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    execute();
        }

        @OnWebSocketMessage("/objectSimpleOnError")
        public void wsEndpointObjectSimpleOnError(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(3);  //objectResponse(null).encoder(new Encoder)
            reply.
                    response().
                    reply().
                    objectResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }
                                System.out.println("return payload after 3 retries");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketError("/objectSimpleOnError")
        public void wsEndpointObjectSimpleOnErrorOnError(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    execute();
        }


        ////----------------- sync objectReply END ------------------------------

        ////----------------- sync stringReply  ------------------------------
        @OnWebSocketMessage("/stringSimpleRetry")
        public void wsEndpointStringSimpleRetry(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(3);  //objectResponse(null).encoder(new Encoder)
            reply.
                    response().
                    reply().
                    stringResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }
                                System.out.println("return payload after 3 retries");
                                return reply.payload().getString().get();
                            }
                    ).retry(3).
                    execute();
            System.out.println("stringSimpleRetry-1: " + name + "   :::" + this);
        }

        @OnWebSocketMessage("/stringUncatchedError")
        public void wsEndpointStringUncatchedError(WebSocketHandler reply) {
            reply.
                    response().
                    reply().
                    stringResponse(() -> {
                                System.out.println("throw");
                                throw new NullPointerException("test");
                            }
                    ).
                    execute();
            System.out.println("stringUncatchedError-1: " + name + "   :::" + this);
        }

        @OnWebSocketError("/stringUncatchedError")
        public void wsEndpointStringUncatchedErrorOnError(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    reply().
                    stringResponse(() -> {

                                System.out.println("return payload after failover");
                                return reply.payload().getString().get();
                            }
                    ).retry(3).
                    execute();
        }

        ////----------------- sync stringReply END ------------------------------

        @OnWebSocketMessage("/uncatchedErrorAsync")
        public void wsEndpointUncatchedErrorAsync(WebSocketHandler reply) {
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {
                                System.out.println("throw");
                                throw new NullPointerException("test");
                            }, new ExampleByteEncoder()
                    ).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketError("/uncatchedErrorAsync")
        public void wsEndpointUncatchedErrorOnErrorAsync(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).retry(3).
                    execute();
        }

        @OnWebSocketMessage("/uncatchedTimeoutErrorAsync")
        public void wsEndpointUncatchedTimeoutErrorAsync(WebSocketHandler reply) {
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {
                                System.out.println("TIMEOUT");
                                Thread.sleep(1000);
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    timeout(500).
                    retry(3).
                    execute();
            System.out.println("binaryReply-1: " + reply.payload().getString().get() + "   :::" + this);
        }

        @OnWebSocketError("/uncatchedTimeoutErrorAsync")
        public void wsEndpointUncatchedTimeoutErrorOnErrorAsync(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    execute();
        }

        @OnWebSocketMessage("/uncatchedTimeoutErrorException")
        public void wsEndpointUncatchedTimeoutErrorException(WebSocketHandler reply) {
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {
                                Thread.sleep(1500);
                                System.out.println("EXCEPTION");
                                throw new NullPointerException("test");
                            }, new ExampleByteEncoder()
                    ).
                    timeout(500).
                    retry(3).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }

        @OnWebSocketError("/uncatchedTimeoutErrorException")
        public void wsEndpointUncatchedTimeoutErrorOnErrorException(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    execute();
        }

        @OnWebSocketMessage("/catchedTimeoutErrorException")
        public void wsEndpointCatchedTimeoutErrorException(WebSocketHandler reply) {
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {
                                System.out.println("SLEEP");
                                Thread.sleep(1500);
                                System.out.println("EXCEPTION");
                                throw new NullPointerException("test");
                            }, new ExampleByteEncoder()
                    ).
                    timeout(1000).
                    onError(error -> {
                        System.out.println("ERROR: ");
                        error.printStackTrace();
                    }).
                    retry(3).
                    onFailureRespond(error -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    execute();
            System.out.println("binaryReply-1: " + name + "   :::" + this);
        }


        @OnWebSocketMessage("/uncatchedMethodError")
        public void wsEndpointUncatchedMethodError(WebSocketHandler reply) {
            throw new NullPointerException("test");
        }

        @OnWebSocketError("/uncatchedMethodError")
        public void wsEndpointUncatchedMethodError(Throwable t, WebSocketHandler reply, WebSocketEndpoint endpoint) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {

                                System.out.println("return payload after failover");
                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).retry(3).
                    execute();
        }

        @OnWebSocketMessage("/catchedError")
        public void wsEndpointCatchedError(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    reply().
                    objectResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }

                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    retry(3).
                    onError((t) -> {
                                if (count.get() <= 1) {
                                    reply.
                                            response().
                                            reply().
                                            objectResponse(() -> {

                                                        System.out.println("fallback");
                                                        t.printStackTrace();
                                                        return new Payload<String>(reply.payload().getString().get());
                                                    }, new ExampleByteEncoder()
                                            ).execute();
                                }
                            }
                    ).
                    execute();
        }

        @OnWebSocketMessage("/catchedErrorAsync")
        public void wsEndpointCatchedErrorAsync(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    blocking().
                    reply().
                    objectResponse(() -> {
                                if (count.decrementAndGet() >= 0) {
                                    System.out.println("throw");
                                    throw new NullPointerException("test");
                                }

                                return new Payload<String>(reply.payload().getString().get());
                            }, new ExampleByteEncoder()
                    ).
                    retry(3).
                    onError((t) -> {
                        System.out.println("error: " + count.get());
                        if (count.get() <= 1) {
                            reply.
                                    response().
                                    reply().
                                    objectResponse(() -> {

                                                System.out.println("fallback: ");
                                                t.printStackTrace();
                                                return new Payload<String>(reply.payload().getString().get());
                                            }, new ExampleByteEncoder()
                                    ).execute();
                        }

                    }).
                    execute();
        }


        @OnWebSocketMessage("/catchedByteError")
        public void wsEndpointCatchedByteError(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    reply().
                    byteResponse(() -> {
                        if (count.decrementAndGet() >= 0) {
                            System.out.println("throw");
                            throw new NullPointerException("test");
                        }

                        return null;
                    }).
                    retry(3).
                    onFailureRespond((t) -> {
                        t.printStackTrace();
                        try {
                            Payload<String> p = new Payload<String>(reply.payload().getString().get());
                            return Serializer.serialize(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return new byte[0];
                    }).
                    execute();
        }

        @OnWebSocketMessage("/catchedStringError")
        public void wsEndpointCatchedStringError(WebSocketHandler reply) {
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    reply().
                    stringResponse(() -> {
                        if (count.decrementAndGet() >= 0) {
                            System.out.println("throw");
                            throw new NullPointerException("test");
                        }

                        return null;
                    }).
                    retry(3).
                    onFailureRespond((t) -> {
                        t.printStackTrace();
                        return reply.payload().getString().get();
                    }).
                    execute();
        }

        @OnWebSocketMessage("/catchedAsyncStringErrorDelay")
        public void wsEndpointCatchedAsyncStringErrorDelay(WebSocketHandler reply) {
            long startTime = System.currentTimeMillis();
            AtomicInteger count = new AtomicInteger(4);
            reply.
                    response().
                    blocking().
                    reply().
                    stringResponse(() -> {
                        long estimatedTime = System.currentTimeMillis() - startTime;
                        System.out.println("time: " + estimatedTime);
                        if (count.decrementAndGet() >= 0) {
                            System.out.println("throw");
                            throw new NullPointerException("test");
                        }

                        return null;
                    }).
                    retry(3).
                    delay(1000).
                    onFailureRespond((t) -> {
                        System.out.print("the stack trace --> ");
                        t.printStackTrace();
                        return reply.payload().getString().get();
                    }).
                    execute();
        }

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
        public void wsEndpointExceptionTests01Error(WebSocketHandler reply, Throwable t) {
            t.printStackTrace();
            System.out.println("----failover");
        }


        @OnWebSocketMessage("/exceptionTests02")
        public void wsEndpointExceptionTests02(WebSocketHandler reply) {
            System.out.println("Exception --XXXX");
            throw new NullPointerException("test");
        }


        @OnWebSocketError("/exceptionTests02---")
        public void wsEndpointExceptionTests02Error(WebSocketHandler reply, Throwable t) {
            t.printStackTrace();
            System.out.println("----failover");
            reply.response().reply().stringResponse(() -> "").execute();
        }


    }


    private void handleInSimpleTests(WebSocket ws, Buffer data) {
        System.out.println("client data simpleRetry:");
        assertNotNull(data.getBytes());
        try {
            Payload<String> payload = (Payload<String>) Serializer.deserialize(data.getBytes());
            assertTrue(payload.equals(new Payload<String>("xhello")));
            System.out.println(payload.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ws.closeHandler((x) -> testComplete());
        ws.close();
    }

    private void handleInSimpleStringTests(WebSocket ws, Buffer data) {
        System.out.println("client data simpleRetry:");
        assertNotNull(data.getBytes());
        String payload = data.getString(0, data.length());
        assertTrue(payload.equals("xhello"));
        System.out.println(payload);
        ws.closeHandler((x) -> testComplete());
        ws.close();

    }
}
