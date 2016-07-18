package org.jacpfx.registry;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLException;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class SecureEtcRegTest extends VertxTestBase {
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

    @Before
    public void startVerticles() throws InterruptedException {



    }

    @Test
    @Ignore
    public void etcdClientTest() throws SSLException {

        HttpClientOptions options = new HttpClientOptions().setSsl(true).setTrustAll(true).setKeyCertOptions(new JksOptions().setPath("/vertx.jks").setPassword("xxxx"));
        vertx.createHttpClient(options).getNow(2379,"localhost","/v2/keys/?recursive=true", response -> response.handler(data -> {
            System.out.println(data);
            testComplete();
        }));
        await();
    }






}

