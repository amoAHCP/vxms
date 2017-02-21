package org.jacpfx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.junit.Test;

/**
 * Created by amo on 15.08.16.
 */
public class AtomicCounterTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    public static final String SERVICE2_REST_GET = "/wsService2";
    private static final String HOST = "localhost";
    public static final int PORT = 9998;
    public static final int PORT2 = 9988;

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

    @Test
    public void testAtomicSyncCounter() {
        vertx.sharedData().getCounter("counter1", resultHandler -> {
            resultHandler.result().compareAndSet(0, 10, handler -> {
                System.out.println(handler.result());
            });
        });

        vertx.sharedData().getCounter("counter1", resultHandler -> {
            resultHandler.result().compareAndSet(10, 11, handler -> {
                System.out.println(handler.result());
                resultHandler.result().get(h -> {
                    System.out.println("10 - 11" + h.result());
                });
            });
        });

        vertx.sharedData().getCounter("counter1", resultHandler -> {
            resultHandler.result().compareAndSet(10, 12, handler -> {
                System.out.println("::" + handler.result());
                resultHandler.result().get(h -> {
                    System.out.println(h.result());
                });
            });
        });

        vertx.sharedData().getLocalMap("bdf").put("1", 1);
        vertx.sharedData().getLocalMap("bdf").put("1", 2);
        System.out.println(vertx.sharedData().getLocalMap("bdf").get("1"));
    }

    @Test
    public void testAtomicSyncCounter2() {
        vertx.sharedData().getCounter("counter1", resultHandler -> {
            resultHandler.result().get(handler -> {
                System.out.println(handler.result());
                vertx.sharedData().getCounter("counter1", r -> {
                    r.result().compareAndSet(10, 11, hh -> {
                        System.out.println(hh.result());
                        r.result().get(h -> {
                            System.out.println(h.result());
                        });
                    });
                });
            });
        });


    }

    @Test
    public void testAtomicSyncCounter3() {
        long max = 3;
        vertx.sharedData().getCounter("counter1", resultHandler -> {
            final Counter counter = resultHandler.result();
            counter.get(handler -> {
                long val = handler.result();
                if (val == 0) {
                    counter.addAndGet(max, hhh-> {
                        System.out.println("::::"+hhh.result());
                    });
                } else {

                }
            });
        });
    }
}
