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



import io.fabric8.annotations.ServiceName;
import io.fabric8.annotations.WithLabel;
import io.fabric8.annotations.WithLabels;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceListBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.test.core.VertxTestBase;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.k8s.annotation.K8SDiscovery;
import org.jacpfx.vxms.k8s.client.VxmsDiscoveryK8SImpl;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

public class ResolveServicesByLabelsTooManyNOKTest extends VertxTestBase {
  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private static final String HOST = "127.0.0.1";
  private HttpClient httpClient;
  public KubernetesMockServer server;
  public Config config;
 // public DefaultKubernetesClient client;

  public void initKubernetes() {
    KubernetesMockServer plainServer = new KubernetesMockServer(false);
    plainServer.init();
    String host = plainServer.getHostName();
    Integer port = plainServer.getPort();
    ClassLoader classLoader = getClass().getClassLoader();
    File ca = new File(classLoader.getResource("ca.crt").getFile());
    File clientcert = new File(classLoader.getResource("client.crt").getFile());
    File clientkey = new File(classLoader.getResource("client.key").getFile());
    System.out.println("port: "+port+"  host:"+host);
    TestingClientConfig.config = new ConfigBuilder()
        .withMasterUrl(host + ":" +port)
        .withNamespace("default")
        .withCaCertFile(ca.getAbsolutePath())
        .withClientCertFile(clientcert.getAbsolutePath())
        .withClientKeyFile(clientkey.getAbsolutePath())
        .build();
  //  client = new DefaultKubernetesClient(config);
    server = plainServer;
  }

  public void initService() {
    final ObjectMeta buildmyTestService = new ObjectMetaBuilder().addToLabels("name", "myTestService").addToLabels("version", "v1").withName("myTestService").build();
    final ServicePort portmyTestService = new ServicePortBuilder().withPort(8080).withProtocol("http").build();
    final ServiceSpec specmyTestService = new ServiceSpecBuilder().addNewPort().and()
        .withClusterIP("192.168.1.1").withPorts(portmyTestService).build();

    final ObjectMeta buildmyTestService2 = new ObjectMetaBuilder().addToLabels("name", "myTestService").addToLabels("version", "v2").withName("myTestService2").build();
    final ServicePort portmyTestService2 = new ServicePortBuilder().withPort(9080).withProtocol("http").build();
    final ServiceSpec specmyTestService2 = new ServiceSpecBuilder().addNewPort().and()
        .withClusterIP("192.168.1.2").withPorts(portmyTestService2).build();

    final Service servicemyTestService = new ServiceBuilder().withMetadata(buildmyTestService).withSpec(specmyTestService).build();
    final Service servicemyTestService2 = new ServiceBuilder().withMetadata(buildmyTestService2).withSpec(specmyTestService2).build();
    server.expect().withPath("/api/v1/namespaces/default/services?labelSelector=name%3DmyTestService").andReturn(200, new ServiceListBuilder().addToItems(servicemyTestService,servicemyTestService2).build()).times(2);

  }
  @Before
  public void startVerticles() throws InterruptedException {
    initKubernetes();
    initService();

  }


  @Test
  public void failedDeploymentNonUnique() throws InterruptedException {
    CountDownLatch latch2 = new CountDownLatch(1);
    DeploymentOptions options = new DeploymentOptions().setInstances(1);

    vertx.deployVerticle(
        new WsServiceOne(config),
        options,
        asyncResult -> {
          System.out.println("start service: " + asyncResult.cause().getMessage());
          assertTrue(asyncResult.failed());

          latch2.countDown();
        });

    httpClient = vertx.createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);
    testComplete();
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  @K8SDiscovery(customClientConfiguration = TestingClientConfig.class)
  public class WsServiceOne extends VxmsEndpoint {

    @ServiceName()
    @WithLabels( value={ @WithLabel(name="name",value="myTestService")})
    private String service1;

    @ServiceName()
    @WithLabels( value={ @WithLabel(name="name",value="myTestService")})
    private String service2;
    public Config config;

    public WsServiceOne(Config config) {this.config =config;}

    public void postConstruct(final io.vertx.core.Future<Void> startFuture) {
      startFuture.complete();
    }

    @Path("/myTestService")
    @GET
    public void rsstringGETResponse(RestHandler reply) {
      System.out.println("stringResponse: " + reply);
      reply.response().stringResponse((future) -> future.complete(service1+"/"+service2)).execute();
    }
  }
}
