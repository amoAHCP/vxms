package org.jacpfx.registry;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.vertx.registry.EtcdRegistration;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class BasicEtcRegTest extends VertxTestBase {
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


    @Test

    public void basicServiceRegistration() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("basicServiceRegistration");
        reg.connect(result -> {
            if (result.succeeded()) {
                reg.retrieveKeys(root -> {
                    System.out.println(root.getNode());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        await();
    }

    @Test

    public void findService() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("findService");

        System.out.println("connect ");
        reg.connect(result -> {
            if (result.succeeded()) {
                reg.retrieveKeys(root -> {
                    org.jacpfx.vertx.registry.Node n1 = findNode(root.getNode(), "/myService");
                    System.out.println(n1);
                    assertEquals("/myService", n1.getKey());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }

    @Test

    public void findService2() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("findService2");


        reg.connect(result -> {
            if (result.succeeded()) {
                reg.findService(service -> {
                    System.out.println("found: " + service);
                    assertEquals("/myService", service.getKey());
                    testComplete();
                }, "/myService");
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        /**  reg.disconnect(res -> {
         System.out.println(":::"+res.statusCode());
         testComplete();

         }); **/

        //  reg.disconnect(Future.factory.future());
        await();
    }

    private org.jacpfx.vertx.registry.Node findNode(org.jacpfx.vertx.registry.Node node, String value) {
        System.out.println("find: " + node.getKey() + "  value:" + value);
        if (node.getKey() != null && node.getKey().equals(value)) return node;
        if (node.isDir()) return node.getNodes().stream().filter(n1 -> {
            org.jacpfx.vertx.registry.Node n2 = n1.isDir() ? findNode(n1, value) : n1;
            return n2.getKey().equals(value);
        }).findFirst().orElse(new org.jacpfx.vertx.registry.Node(false, "", "", "", 0, 0, 0, Collections.emptyList()));
        return null;
    }


    public HttpClient getClient() {
        return getVertx().
                createHttpClient(new HttpClientOptions());
    }


}
