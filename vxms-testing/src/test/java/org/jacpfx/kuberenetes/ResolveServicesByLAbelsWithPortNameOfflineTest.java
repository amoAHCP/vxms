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

package org.jacpfx.kuberenetes;

import io.fabric8.annotations.PortName;
import io.fabric8.annotations.ServiceName;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.k8s.annotation.K8SDiscovery;
import org.jacpfx.vxms.k8s.client.VxmsDiscoveryK8SImpl;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

public class ResolveServicesByLAbelsWithPortNameOfflineTest extends VertxTestBase {
  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private static final String HOST = "127.0.0.1";
  public KubernetesMockServer server;
  public Config config;
  private HttpClient httpClient;
  // public DefaultKubernetesClient client;

  public void initKubernetes() {

    ClassLoader classLoader = getClass().getClassLoader();
    File ca = new File(classLoader.getResource("ca.crt").getFile());
    File clientcert = new File(classLoader.getResource("client.crt").getFile());
    File clientkey = new File(classLoader.getResource("client.key").getFile());
    System.out.println("port: " + 0 + "  host:" + 0);
    config =
        new ConfigBuilder()
            .withMasterUrl(0 + ":" + 0)
            .withNamespace(null)
            .withCaCertFile(ca.getAbsolutePath())
            .withClientCertFile(clientcert.getAbsolutePath())
            .withClientKeyFile(clientkey.getAbsolutePath())
            .build();
  }

  @Before
  public void startVerticles() throws InterruptedException {
    initKubernetes();
    CountDownLatch latch2 = new CountDownLatch(1);
    JsonObject conf = new JsonObject();
    conf.put("service1.name", "myTestService").put("service1.port", "mytcp");
    conf.put("service2.name", "myTestService2");
    conf.put("kube.offline", true);
    conf.put("myTestService", "tcp://192.168.1.1");
    conf.put("myTestService2", "http://192.168.1.2:9080");
    conf.put("mytcp", "9090");
    DeploymentOptions options = new DeploymentOptions().setConfig(conf).setInstances(1);

    vertx.deployVerticle(
        new WsServiceOne(config),
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

    httpClient = vertx.createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);
  }

  @Test
  public void testServiceByName() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicBoolean failed = new AtomicBoolean(false);
    HttpClientOptions options = new HttpClientOptions();
    options.setDefaultPort(PORT);
    options.setDefaultHost(HOST);
    HttpClient client = vertx.createHttpClient(options);
    HttpClientRequest request =
        client.get(
            "/wsService/myTestService",
            resp -> {
              resp.bodyHandler(
                  body -> {
                    String response = body.toString();
                    System.out.println("Response entity '" + response + "' received.");
                    vertx.runOnContext(
                        context -> {
                          failed.set(
                              !response.equalsIgnoreCase(
                                  "tcp://192.168.1.1:9090/http://192.168.1.2:9080"));

                          latch.countDown();
                        });
                  });
            });
    request.end();

    latch.await();
    assertTrue(!failed.get());
    testComplete();
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  @K8SDiscovery
  public class WsServiceOne extends VxmsEndpoint {

    public Config config;
    @ServiceName("${service1.name}")
    @PortName("${service1.port}")
    private String service1;
    @ServiceName("${service2.name}")
    private String service2;

    public WsServiceOne(Config config) {
      this.config = config;
    }

    public void postConstruct(final io.vertx.core.Future<Void> startFuture) {
      new VxmsDiscoveryK8SImpl().initDiscovery(this, config);
      startFuture.complete();
    }

    @Path("/myTestService")
    @GET
    public void rsstringGETResponse(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply
          .response()
          .stringResponse((future) -> future.complete(service1 + "/" + service2))
          .execute();
    }
  }
}
