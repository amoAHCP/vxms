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
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 20.06.16.
 */
public class EtcdRegistrationBasicTest extends VertxTestBase

{
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
        getVertx().deployVerticle(new EtcdAwareService(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service EtcdAwareService: " + asyncResult.succeeded());
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
    public void testEtcdDiscoveryClientAndConnect() {
      /**  EtcdAwareService service = new EtcdAwareService();
        service.init(vertx, vertx.getOrCreateContext());
        final DiscoveryClient client = DiscoveryClient.createClient(service);**/
        final DiscoveryClient client = DiscoveryClient.createClient(vertx,new HttpClientOptions(),
                new JsonObject().put("etcd-host","127.0.0.1").put("etcd-port","4001").put("etcd-domain","etcdAwareTest"));

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




    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    @EtcdClient(domain = "etcdAwareTest", host = "127.0.0.1", port = 4001, ttl = 10, exportedHost = "127.0.0.1")
    public class EtcdAwareService extends VxmsEndpoint {

        public void postConstruct(final Future<Void> startFuture) {
            System.out.println("POSTCONSTRUCT");
            startFuture.complete();
        }

        @Path("/simpleRESTEndpoint")
        @GET
        public void simpleRESTEndpoint(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse((future) -> future.complete("test-123")).execute();
        }

    }



}
