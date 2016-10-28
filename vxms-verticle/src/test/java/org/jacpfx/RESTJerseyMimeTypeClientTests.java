package org.jacpfx;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyMimeTypeClientTests extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
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

        client = getVertx().
                createHttpClient(new HttpClientOptions());
        awaitLatch(latch2);

    }


    @Test

    public void stringGETResponse() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringGETConsumesResponse/123");
        Future<String> getCallback = target.request().header("Content-Type", "application/json;charset=UTF-8").async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"123");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println("getCallback");
                   throwable.printStackTrace();
            }
        });

        latch.await();
        CountDownLatch latch2 = new CountDownLatch(1);
        Future<String> getCallback2 = target.request(MediaType.APPLICATION_ATOM_XML).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"123");

            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println("getCallback2");
                throwable.printStackTrace();
                latch2.countDown();

            }
        });
        latch2.await();
        CountDownLatch latch3 = new CountDownLatch(1);

        Future<String> getCallback3 = target.request().header("Content-Type", "application/xml;charset=UTF-8").async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received in getCallback3.");
                Assert.assertEquals(response,"123");
                latch3.countDown();
            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println("getCallback");
                throwable.printStackTrace();
            }
        });

        latch3.await();
        testComplete();

    }


    @Test

    public void stringPOSTResponse() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringPOSTConsumesResponse/123");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.entity("hello", MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"hello");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println("getCallback");
                throwable.printStackTrace();
            }
        });

        latch.await();
        CountDownLatch latch2 = new CountDownLatch(1);
        Future<String> getCallback2 = target.request(MediaType.APPLICATION_ATOM_XML).async().post(Entity.entity("hello", MediaType.APPLICATION_ATOM_XML), new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"hello");

            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println("getCallback2");
                throwable.printStackTrace();
                latch2.countDown();

            }
        });
        latch2.await();
        CountDownLatch latch3 = new CountDownLatch(1);

        Future<String> getCallback3 = target.request(MediaType.APPLICATION_XML).async().post(Entity.entity("hello", MediaType.APPLICATION_XML), new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received in getCallback3.");
                Assert.assertEquals(response,"hello");
                latch3.countDown();
            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println("getCallback");
                throwable.printStackTrace();
            }
        });

        latch3.await();
        testComplete();

    }


    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {

        @Path("/stringGETConsumesResponse/:myJSON")
        @GET
        @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
        public void rsstringGETResponse(RestHandler handler) {
            final String myJSON = handler.request().param("myJSON");
            System.out.println("stringResponse: " + myJSON);
            handler.response().stringResponse((future) -> future.complete(myJSON)).execute();
        }


        @Path("/stringPOSTConsumesResponse/:myJSON")
        @POST
        @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
        public void rsstringPOSTResponse(RestHandler handler) {
            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringPOSTResponse: " + val);
            handler.response().stringResponse((future) -> future.complete(val)).execute();
        }



    }

}
