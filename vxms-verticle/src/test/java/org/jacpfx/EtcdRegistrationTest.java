package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.etcd.client.EtcdClient;
import org.jacpfx.vertx.registry.DiscoveryClient;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 20.06.16.
 */
public class EtcdRegistrationTest extends VertxTestBase

{
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    public static final String SERVICE_REST_GET_2 = "/EtcdAwareServiceA";
    public static final String SERVICE_REST_GET_3 = "/EtcdAwareServiceB";
    private static final String HOST = "localhost";
    public static final int PORT = 9998;
    public static final int PORT_2 = 9997;
    public static final int PORT_3 = 9996;

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


        CountDownLatch latch2 = new CountDownLatch(3);
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
        getVertx().deployVerticle(new EtcdAwareService(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            if (asyncResult.failed()) {
                // Test should proceed on connection error
                System.out.println("failed; " + asyncResult.cause().getMessage());
                assertTrue(asyncResult.cause().getMessage().toString().contains("Connection refused:"));
            } else {
                System.out.println("start service true: " + asyncResult.succeeded());
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
            }


            // If deployed correctly then start the tests!
            //   latch2.countDown();
            System.out.println("countdown ");
            latch2.countDown();

        });
        getVertx().deployVerticle(new EtcdAwareServiceA(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            if (asyncResult.failed()) {
                // Test should proceed on connection error
                System.out.println("failed; " + asyncResult.cause().getMessage());
                assertTrue(asyncResult.cause().getMessage().toString().contains("Connection refused:"));
            } else {
                System.out.println("start service true: " + asyncResult.succeeded());
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
            }


            // If deployed correctly then start the tests!
            //   latch2.countDown();
            System.out.println("countdown ");
            latch2.countDown();

        });
        getVertx().deployVerticle(new EtcdAwareServiceB(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            if (asyncResult.failed()) {
                // Test should proceed on connection error
                System.out.println("failed; " + asyncResult.cause().getMessage());
                assertTrue(asyncResult.cause().getMessage().toString().contains("Connection refused:"));
            } else {
                System.out.println("start service true: " + asyncResult.succeeded());
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
            }


            // If deployed correctly then start the tests!
            //   latch2.countDown();
            System.out.println("countdown ");
            latch2.countDown();

        });
        System.out.println("create client ");
        client = getVertx().
                createHttpClient(new HttpClientOptions());
        System.out.println("await ");
        awaitLatch(latch2);

    }


    @Test
    public void testSuccsessfulRegistration() {

    }

    @Test
    public void testEtcdDiscoveryClient() {
        EtcdAwareService service = new EtcdAwareService();
        service.init(vertx, vertx.getOrCreateContext());
        final DiscoveryClient client = DiscoveryClient.createClient(service);
        if (client.isConnected()) {
            client.find(SERVICE_REST_GET).onSuccess(val -> {
                System.out.println(" found node : " + val.getServiceNode());
                System.out.println(" found URI : " + val.getServiceNode().getUri().toString());
                testComplete();

            }).onError(error -> {
                System.out.println("error: " + error.getThrowable().getMessage());
            }).onFailure(node -> {
                System.out.println("not found");
                testComplete();
            }).retry(2).execute();


            //  reg.disconnect(Future.factory.future());
            await();
        } else {
            testComplete();
        }

    }


    @Test
    public void testEtcdDiscoveryClientAndConnect() {
        EtcdAwareService service = new EtcdAwareService();
        service.init(vertx, vertx.getOrCreateContext());
        final DiscoveryClient client = DiscoveryClient.createClient(service);
        if (client.isConnected()) {
            client.find(SERVICE_REST_GET).onSuccess(val -> {
                HttpClientOptions options = new HttpClientOptions();
                HttpClient httpclient = vertx.
                        createHttpClient(options);

                HttpClientRequest request = httpclient.getAbs(val.getServiceNode().getUri().toString() + "/simpleRESTEndpoint/", resp -> {
                    if(resp.statusCode()==200) {
                        resp.bodyHandler(body -> {

                            System.out.println("Got a createResponse: " + body.toString());
                            assertTrue("test-123".equals(body.toString()));
                            testComplete();
                        });
                    } else {
                        testComplete();
                    }
                });

                request.end();

            }).onError(error -> {
                System.out.println("error: " + error.getThrowable().getMessage());
            }).onFailure(node -> {
                System.out.println("not found");
                testComplete();
            }).retry(2).execute();


            //  reg.disconnect(Future.factory.future());
            await();
        } else {
            testComplete();
        }

    }

    @Test
    public void testEtcdDiscoveryClientRequestChain() {
        final DiscoveryClient client = DiscoveryClient.createClient(vertx,new HttpClientOptions(), new JsonObject().put("etcd-host","127.0.0.1").put("etcd-port","4001").put("etcd-domain","etcdAwareTest"));
        if (client.isConnected()) {
            client.find(SERVICE_REST_GET_2).onSuccess(val -> {
                HttpClientOptions options = new HttpClientOptions();
                HttpClient httpclient = vertx.
                        createHttpClient(options);

                HttpClientRequest request = httpclient.getAbs(val.getServiceNode().getUri().toString() + "/simpleRESTEndpoint/hello", resp -> {
                    if(resp.statusCode()==200) {
                        resp.bodyHandler(body -> {
                            System.out.println("Got a createResponse: " + body.toString());
                            assertTrue("test-123hello".equals(body.toString()));
                            testComplete();
                        });
                    } else {
                        testComplete();
                    }
                });

                request.end();

            }).onError(error -> {
                System.out.println("error: " + error.getThrowable().getMessage());
            }).onFailure(node -> {
                System.out.println("not found");
                testComplete();
            }).retry(2).execute();


            //  reg.disconnect(Future.factory.future());
            await();
        } else {
            testComplete();
        }

    }


    @ServiceEndpoint(name = SERVICE_REST_GET, port = PORT)
    @EtcdClient(domain = "etcdAwareTest", host = "127.0.0.1", port = 4001, ttl = 10)
    public class EtcdAwareService extends VxmsEndpoint {

        public void postConstruct(final Future<Void> startFuture) {
            startFuture.complete();
        }

        @Path("/simpleRESTEndpoint")
        @GET
        public void simpleRESTEndpoint(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse((future) -> future.complete("test-123")).execute();
        }

    }

    @ServiceEndpoint(name = SERVICE_REST_GET_2, port = PORT_2)
    @EtcdClient(domain = "etcdAwareTest", host = "127.0.0.1", port = 4001, ttl = 10)
    public class EtcdAwareServiceA extends VxmsEndpoint {
        private DiscoveryClient client;
        private HttpClient httpclient;
        public void postConstruct(final Future<Void> startFuture) {
            client = DiscoveryClient.createClient(this);

            HttpClientOptions options = new HttpClientOptions();
            httpclient = vertx.
                    createHttpClient(options);
            if(client.isConnected()) {
                startFuture.complete();
            }else {
                startFuture.fail("no connection to discovery service");
            }
        }

        @Path("/simpleRESTEndpoint/:value")
        @GET
        public void simpleRESTEndpoint(RestHandler reply) {

            client.find(SERVICE_REST_GET_3).onSuccess(node -> {
                HttpClientRequest request = httpclient.
                        getAbs(node.getServiceNode().getUri().toString() + "/simpleRESTEndpoint/"+reply.request().param("value"), resp -> {
                    if(resp.statusCode()==200) {
                        resp.bodyHandler(body -> {
                            System.out.println("Got a createResponse in EtcdAwareServiceA: " + body.toString());
                            reply.response().stringResponse((future)->future.complete(body.toString())).execute();
                        });
                    } else {
                        // do something
                    }
                });

                request.end();
            }).onError((e) -> {
                System.out.println(e.getThrowable().getMessage());
            }).execute();
            System.out.println("stringResponse: " + reply);
        }

    }

    @ServiceEndpoint(name = SERVICE_REST_GET_3, port = PORT_3)
    @EtcdClient(domain = "etcdAwareTest", host = "127.0.0.1", port = 4001, ttl = 10)
    public class EtcdAwareServiceB extends VxmsEndpoint {
         private DiscoveryClient client;
        public void postConstruct(final Future<Void> startFuture) {
            client = DiscoveryClient.createClient(this);
            if(client.isConnected()) {
                startFuture.complete();
            }else {
                startFuture.fail("no connection to discovery service");
            }

        }

        @Path("/simpleRESTEndpoint/:value")
        @GET
        public void simpleRESTEndpoint(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse((future) -> future.complete("test-123"+reply.request().param("value"))).execute();
        }

    }

}
