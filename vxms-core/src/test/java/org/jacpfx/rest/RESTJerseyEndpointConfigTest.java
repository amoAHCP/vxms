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


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.FileUpload;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.entity.MyCustomRouterConfig;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyEndpointConfigTest extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private final static int MAX_RESPONSE_ELEMENTS = 4;
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
    // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
    // don'failure have to hardecode it in your tests

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
  public void stringPOSTResponseWithParameter()
      throws InterruptedException, ExecutionException, IOException {
    final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
    File file = new File(getClass().getClassLoader().getResource("payload.xml").getFile());
    final FileDataBodyPart filePart = new FileDataBodyPart("file", file);
    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
    final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar")
        .field("hello", "world").bodyPart(filePart);

    WebTarget target = client.target("http://" + HOST + ":" + PORT)
        .path("/wsService/simpleFilePOSTupload");
    final Response response = target.request()
        .post(Entity.entity(multipart, multipart.getMediaType()));

    //Use createResponse object to verify upload success
    final String entity = response.readEntity(String.class);
    System.out.println(entity);
    assertTrue(entity.equals("barworlddfgdfg"));
    formDataMultiPart.close();
    multipart.close();
    testComplete();

  }


  public HttpClient getClient() {
    return client;
  }


  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT, routerConf = MyCustomRouterConfig.class)
  public class WsServiceOne extends VxmsEndpoint {


    @Path("/simpleFilePOSTupload")
    @POST
    public void rsstringPOSTResponse(RestHandler handler) {
      handler.response().blocking().stringResponse(() -> {
        Set<FileUpload> files = handler.request().fileUploads();
        System.out.println("FILES: " + files + "   " + handler.request().param("foo"));
        FileUpload f = files.iterator().next();
        System.out.println("name: " + f.fileName() + "  ");
        Buffer uploaded = vertx.fileSystem().readFileBlocking(f.uploadedFileName());
        Document payload = obtenerDocumentDeByte(uploaded.getBytes());
        String payloadValue = payload.getElementsByTagName("test").item(0).getTextContent();
        System.out
            .println("payload:  " + payload.getElementsByTagName("test").item(0).getTextContent());
        return handler.request().param("foo") + handler.request().param("hello") + payloadValue;
      }).execute();
    }

    private Document obtenerDocumentDeByte(byte[] documentoXml) throws Exception {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(documentoXml));
    }

  }


}
