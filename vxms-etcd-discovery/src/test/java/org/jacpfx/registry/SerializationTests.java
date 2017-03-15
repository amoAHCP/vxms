package org.jacpfx.registry;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.vertx.registry.nodes.Node;
import org.jacpfx.vertx.registry.nodes.Root;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by amo on 15.08.16.
 */
public class SerializationTests extends VertxTestBase {
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
    public void testStringSerialization() {
        SharedData sharedData = vertx.sharedData();
        final LocalMap<String, String> cache = sharedData.getLocalMap("test1");
        long startTime = System.currentTimeMillis();

        for(int i=0; i<1000000; i++) {
            cache.put(String.valueOf(i), Json.encode(new Root("ssd", new Node(false,"1","","",10,10,10, Collections.emptyList()),new Node(false,"1","","",10,10,10, Collections.emptyList()),1,"","",1)));
        }

        for(int i=0; i<1000000; i++) {
            Root r = Json.decodeValue(cache.get(String.valueOf(i)),Root.class);
        }

        long estimatedTime = System.currentTimeMillis() - startTime;

        System.out.println("time: "+estimatedTime);
    }
    @Test
    public void testSerializableSerialization() {
        SharedData sharedData = vertx.sharedData();
        final LocalMap<String, Root> cache = sharedData.getLocalMap("test2");
        long startTime = System.currentTimeMillis();

        for(int i=0; i<1000000; i++) {
            cache.put(String.valueOf(i), new Root("ssd", new Node(false,"1","","",10,10,10, Collections.emptyList()),new Node(false,"1","","",10,10,10, Collections.emptyList()),1,"","",1));
        }

        for(int i=0; i<1000000; i++) {
            Root r = cache.get(String.valueOf(i));
        }
        long estimatedTime = System.currentTimeMillis() - startTime;

        System.out.println("time: "+estimatedTime);
    }
}
