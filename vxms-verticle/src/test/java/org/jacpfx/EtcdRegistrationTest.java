package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
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
            System.out.println("start service: " + asyncResult.succeeded());
            if(asyncResult.failed()) {
                assertTrue(asyncResult.cause().getMessage().toString().contains("Connection refused:"));
            } else {
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
            }


            // If deployed correctly then start the tests!
            //   latch2.countDown();

            latch2.countDown();

        });

        client = getVertx().
                createHttpClient(new HttpClientOptions());
        awaitLatch(latch2);

    }


    @Test
    public void testSuccsessfulRegistration() {

    }
    @Test
    public void testEtcdDiscoveryClient() {
        EtcdAwareService service = new EtcdAwareService();
        service.init(vertx,vertx.getOrCreateContext());
        final DiscoveryClient client = DiscoveryClient.createClient(service);
        if(client.isConnected()){
            client.find(SERVICE_REST_GET).onSuccess(val->{
                System.out.println(" found node : "+val.getServiceNode());
                System.out.println(" found URI : "+val.getServiceNode().getUri().toString());
                testComplete();

            }).onError(error->{
                System.out.println("error: "+error.getThrowable().getMessage());
            }).onFailure(node->{
                System.out.println("not found");
                testComplete();
            }).retry(2).execute();


            //  reg.disconnect(Future.factory.future());
            await();
        }

    }

    @ServiceEndpoint(name = SERVICE_REST_GET, port = PORT)
    @EtcdClient(domain = "etcdAwareTest",host = "127.0.0.1",port = 4001,ttl = 30)
    public class EtcdAwareService extends VxmsEndpoint
    {

        public void postConstruct(final Future<Void> startFuture) {

            startFuture.complete();
        }
        @Path("/simpleRESTEndpoint")
        @GET
        public void simpleRESTEndpoint(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse(() -> "test-123").execute();
        }

    }

}