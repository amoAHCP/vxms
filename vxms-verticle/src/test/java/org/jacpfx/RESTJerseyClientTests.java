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
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientTests extends VertxTestBase {
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
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringGETResponse");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"test");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        });

        latch.await();
        testComplete();

    }

    @Test
    public void stringPOST() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringPOST");
        target.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.entity("hello", MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<String>() {
            @Override
            public void completed(String response) {

                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        }).get();

        latch.await();
        testComplete();

    }


    @Ignore // TODO add autoclose after method execution... be aware of blocking processes in background
    @Test
    public void stringPOSTNoEnd() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringPOSTNoEnd");
        target.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.entity("hello", MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<String>() {
            @Override
            public void completed(String response) {

                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        }).get();

        latch.await();
        testComplete();

    }

    @Test
    public void stringPOSTResponse() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringPOSTResponse");
       target.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.entity("hello", MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<String>() {
           @Override
           public void completed(String response) {
               System.out.println("Response entity '" + response + "' received.");
               Assert.assertEquals(response,"hello");
               latch.countDown();
           }

           @Override
           public void failed(Throwable throwable) {

           }
       }).get();

        latch.await();
        testComplete();

    }

    @Test
    public void stringOPTIONSResponse() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringOPTIONSResponse");
        target.request(MediaType.APPLICATION_JSON_TYPE).async().options( new InvocationCallback<String>() {
            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"hello");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        }).get();

        latch.await();
        testComplete();

    }

    @Test
    public void stringPUTResponse() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringPUTResponse");
        target.request(MediaType.APPLICATION_JSON_TYPE).async().put(Entity.entity("hello", MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<String>() {
            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"hello");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        }).get();

        latch.await();
        testComplete();

    }



    @Test
    public void stringDELETEResponse() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringDELETEResponse");
        target.request(MediaType.APPLICATION_JSON_TYPE).async().delete( new InvocationCallback<String>() {
            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"hello");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        }).get();

        latch.await();
        testComplete();

    }

    @Test
    public void stringGETResponseWithParameter() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringGETResponseWithParameter/123");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"123");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        });

        latch.await();
        testComplete();


    }

    @Test
    public void stringPOSTResponseWithParameter() throws InterruptedException, ExecutionException {

        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + HOST + ":" +PORT).path("/wsService/stringPOSTResponseWithParameter/123");
        target.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.entity("hello", MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<String>() {
            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response,"hello:123");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {

            }
        }).get();

        latch.await();
        testComplete();


    }


    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(name = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {

        @Path("/stringGETResponse")
        @GET
        public void rsstringGETResponse(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse(() -> "test").execute();
        }

        @Path("/stringPOSTResponse")
        @POST
        public void rsstringPOSTResponse(RestHandler handler) {

            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringPOSTResponse: " + val);
            handler.response().stringResponse(() -> val).execute();
        }

        @Path("/stringOPTIONSResponse")
        @OPTIONS
        public void rsstringOPTIONSResponse(RestHandler handler) {

            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringOPTIONSResponse: " + val);
            handler.response().stringResponse(() -> "hello").execute();
        }

        @Path("/stringPUTResponse")
        @PUT
        public void rsstringPUTResponse(RestHandler handler) {

            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringPUTResponse: " + val);
            handler.response().stringResponse(() -> val).execute();
        }

        @Path("/stringDELETEResponse")
        @DELETE
        public void rsstringDELETEResponse(RestHandler handler) {

            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringDELETEResponse: " + val);
            handler.response().stringResponse(() -> "hello").execute();
        }



        @Path("/stringPOST")
        @POST
        public void rsstringPOST(RestHandler handler) {

            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringPOST: " + val);
            handler.response().end();
        }

        @Path("/stringPOSTNoEnd")
        @POST
        public void rsstringPOSTNoEnd(RestHandler handler) {

            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringPOSTNoEnd: " + val);
        }

        @Path("/stringGETResponseWithParameter/:help")
        @GET
        public void rsstringGETResponseWithParameter(RestHandler handler) {
            String productType = handler.request().param("help");
            System.out.println("wsEndpointTwo: " + handler);
            handler.response().stringResponse(() -> productType).execute();
        }
        @Path("/stringPOSTResponseWithParameter/:help")
        @POST
        public void rsstringPOSTResponseWithParameter(RestHandler handler) {
            String productType = handler.request().param("help");
            String val = handler.request().body().getString(0,handler.request().body().length());
            System.out.println("stringPOSTResponse: " + val+":"+productType);
            handler.response().stringResponse(() -> val+":"+productType).execute();
        }

    }

}
