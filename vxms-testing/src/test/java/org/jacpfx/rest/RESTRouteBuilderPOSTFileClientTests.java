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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.FileUpload;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.base.RouteBuilder;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/** Created by Andy Moncsek on 23.04.15. */
public class RESTRouteBuilderPOSTFileClientTests extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 9998;
  private static final int MAX_RESPONSE_ELEMENTS = 4;
  private static final String HOST = "127.0.0.1";

  static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

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

    awaitLatch(latch2);
  }

  @Test
  public void stringPOSTResponseWithParameter()
      throws InterruptedException, ExecutionException, IOException {
    File file = new File(getClass().getClassLoader().getResource("payload.xml").getFile());
    HttpPost post =
        new HttpPost("http://" + HOST + ":" + PORT + SERVICE_REST_GET + "/simpleFilePOSTupload");
    HttpClient client = HttpClientBuilder.create().build();

    FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
    StringBody stringBody1 = new StringBody("bar", ContentType.MULTIPART_FORM_DATA);
    StringBody stringBody2 = new StringBody("world", ContentType.MULTIPART_FORM_DATA);
    //
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    builder.addPart("file", fileBody);
    builder.addPart("foo", stringBody1);
    builder.addPart("hello", stringBody2);
    HttpEntity entity = builder.build();
    //
    post.setEntity(entity);
    HttpResponse response = client.execute(post);

    // Use createResponse object to verify upload success
    final String entity1 = convertStreamToString(response.getEntity().getContent());
    System.out.println("-->" + entity1);
    assertTrue(entity1.equals("barworlddfgdfg"));

    testComplete();
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends AbstractVerticle {

    @Override
    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
      VxmsRESTRoutes routes =
              VxmsRESTRoutes.init()
                      .route(RouteBuilder.post("/simpleFilePOSTupload", this::rsstringPOSTResponse));
      VxmsEndpoint.init(startFuture, this, routes);
    }

    public void rsstringPOSTResponse(RestHandler handler) {
      handler
          .response()
          .blocking()
          .stringResponse(
              () -> {
                Set<FileUpload> files = handler.request().fileUploads();
                System.out.println("FILES: " + files + "   " + handler.request().param("foo"));
                FileUpload f = files.iterator().next();
                System.out.println("name: " + f.fileName() + "  ");
                Buffer uploaded = vertx.fileSystem().readFileBlocking(f.uploadedFileName());
                Document payload = obtenerDocumentDeByte(uploaded.getBytes());
                String payloadValue = payload.getElementsByTagName("test").item(0).getTextContent();
                System.out.println(
                    "payload:  " + payload.getElementsByTagName("test").item(0).getTextContent());
                return handler.request().param("foo")
                    + handler.request().param("hello")
                    + payloadValue;
              })
          .execute();
    }

    private Document obtenerDocumentDeByte(byte[] documentoXml) throws Exception {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(documentoXml));
    }
  }
}
