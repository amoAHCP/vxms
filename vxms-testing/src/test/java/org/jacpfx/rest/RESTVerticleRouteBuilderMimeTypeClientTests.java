/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.RouteBuilder;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTVerticleRouteBuilderMimeTypeClientTests extends VertxTestBase {

    public static final String SERVICE_REST_GET = "/wsService";
    public static final int PORT = 9998;
    private static final int MAX_RESPONSE_ELEMENTS = 4;
    private static final String HOST = "127.0.0.1";
    private HttpClient client;

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
        // Deploy the module - the System property `vertx.modulename` will contain the name of the
        // module so you
        // don'failure have to hardecode it in your tests

        getVertx()
                .deployVerticle(
                        new WsServiceOne(),
                        options,
                        asyncResult -> {
                            // Deployment is asynchronous and this this handler will be called when it's complete
                            // (or failed)
                            System.out.println("start service: " + asyncResult.succeeded());
                            assertTrue(asyncResult.succeeded());
                            assertNotNull("deploymentID should not be null", asyncResult.result());
                            // If deployed correctly then start the tests!
                            //   latch2.countDown();

                            latch2.countDown();
                        });

        client = getVertx().createHttpClient(new HttpClientOptions());
        awaitLatch(latch2);
    }

    @Test
    public void stringGETResponse_1() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request =
                client
                        .get(
                                "/wsService/stringGETConsumesResponse/123",
                                resp -> {
                                    resp.exceptionHandler(error -> {
                                    });

                                    resp.bodyHandler(
                                            body -> {
                                                System.out.println("Got a createResponse: " + body.toString());
                                                assertEquals(body.toString(), "123");
                                                testComplete();
                                            });
                                })
                        .putHeader("Content-Type", "application/json;charset=UTF-8");
        request.end();
        await();
    }

    @Test
    public void stringGETResponse_2() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request =
                client
                        .get(
                                "/wsService/stringGETConsumesResponse/123",
                                resp -> {
                                    resp.exceptionHandler(
                                            error -> {
                                                System.out.println("Got a createResponse ERROR: " + error.toString());
                                            });
                                    resp.bodyHandler(
                                            body -> {
                                                System.out.println("Got a createResponse: " + body.toString());
                                                assertEquals(
                                                        body.toString(),
                                                        "<html><body><h1>Resource not found</h1></body></html>");
                                                testComplete();
                                            });
                                })
                        .putHeader("Content-Type", MediaType.APPLICATION_ATOM_XML);
        request.end();
        await();
    }

    @Test
    public void stringGETResponse_3() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request =
                client
                        .get(
                                "/wsService/stringGETConsumesResponse/123",
                                resp -> {
                                    resp.exceptionHandler(
                                            error -> {
                                                System.out.println("Got a createResponse ERROR: " + error.toString());
                                            });
                                    resp.bodyHandler(
                                            body -> {
                                                System.out.println("Got a createResponse: " + body.toString());
                                                assertEquals(body.toString(), "123");
                                                testComplete();
                                            });
                                })
                        .putHeader("Content-Type", "application/xml;charset=UTF-8");
        request.end();
        await();
    }

    @Test
    public void stringPOSTResponse_1() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request =
                client
                        .post(
                                "/wsService/stringPOSTConsumesResponse/123",
                                resp -> {
                                    resp.exceptionHandler(error -> {
                                    });

                                    resp.bodyHandler(
                                            body -> {
                                                System.out.println("Got a createResponse: " + body.toString());
                                                assertEquals(body.toString(), "hello");
                                                testComplete();
                                            });
                                })
                        .putHeader("content-length", String.valueOf("hello".getBytes().length))
                        .putHeader("Content-Type", "application/json;charset=UTF-8");
        request.write("hello");
        request.end();
        await();
    }

    @Test
    public void stringPOSTResponse_2() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request =
                client
                        .post(
                                "/wsService/stringPOSTConsumesResponse/123",
                                resp -> {
                                    resp.exceptionHandler(error -> {
                                    });

                                    resp.bodyHandler(
                                            body -> {
                                                System.out.println("Got a createResponse: " + body.toString());
                                                assertEquals(
                                                        body.toString(),
                                                        "<html><body><h1>Resource not found</h1></body></html>");
                                                testComplete();
                                            });
                                })
                        .putHeader("content-length", String.valueOf("hello".getBytes().length))
                        .putHeader("Content-Type", MediaType.APPLICATION_ATOM_XML);
        request.write("hello");
        request.end();
        await();
    }

    @Test
    public void stringPOSTResponse_3() throws InterruptedException {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(PORT);
        options.setDefaultHost(HOST);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request =
                client
                        .post(
                                "/wsService/stringPOSTConsumesResponse/123",
                                resp -> {
                                    resp.exceptionHandler(error -> {
                                    });

                                    resp.bodyHandler(
                                            body -> {
                                                System.out.println("Got a createResponse: " + body.toString());
                                                assertEquals(body.toString(), "hello");
                                                testComplete();
                                            });
                                })
                        .putHeader("content-length", String.valueOf("hello".getBytes().length))
                        .putHeader("Content-Type", MediaType.APPLICATION_XML);
        request.write("hello");
        request.end();
        await();
    }

    public HttpClient getClient() {
        return client;
    }

    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends AbstractVerticle {

        @Override
        public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
            VxmsRESTRoutes routes =
                    VxmsRESTRoutes.init()
                            .route(RouteBuilder.get("/stringGETConsumesResponse/:myJSON", this::rsstringGETResponse, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))
                            .route(RouteBuilder.post("/stringPOSTConsumesResponse/:myJSON", this::rsstringPOSTResponse, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
            VxmsEndpoint.init(startFuture, this, routes);
        }

        public void rsstringGETResponse(RestHandler handler) {
            final String myJSON = handler.request().param("myJSON");
            System.out.println("stringResponse: " + myJSON);
            handler.response().stringResponse((future) -> future.complete(myJSON)).execute();
        }

        public void rsstringPOSTResponse(RestHandler handler) {
            String val = handler.request().body().getString(0, handler.request().body().length());
            System.out.println("stringPOSTResponse: " + val);
            handler.response().stringResponse((future) -> future.complete(val)).execute();
        }
    }
}
