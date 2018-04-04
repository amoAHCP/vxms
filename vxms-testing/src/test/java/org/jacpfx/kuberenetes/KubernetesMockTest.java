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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServiceListBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class KubernetesMockTest {

  public KubernetesMockServer server;
  public Config config;
  public DefaultKubernetesClient client;

  @Before
  public void init() {
    KubernetesMockServer plainServer = new KubernetesMockServer(false);
    plainServer.init();
    String host = plainServer.getHostName();
    Integer port = plainServer.getPort();
    ClassLoader classLoader = getClass().getClassLoader();
    File ca = new File(classLoader.getResource("ca.crt").getFile());
    File clientcert = new File(classLoader.getResource("client.crt").getFile());
    File clientkey = new File(classLoader.getResource("client.key").getFile());
    System.out.println("port: "+port+"  host:"+host);
    config = new ConfigBuilder()
        .withMasterUrl(host + ":" +port)
        .withCaCertFile(ca.getAbsolutePath())
        .withClientCertFile(clientcert.getAbsolutePath())
        .withClientKeyFile(clientkey.getAbsolutePath())
        .build();
    client = new DefaultKubernetesClient(config);
    server = plainServer;
  }

  @Test
  public void findServices() {
    final ObjectMeta build = new ObjectMetaBuilder().addToLabels("test", "test").build();
    final ServiceSpec spec = new ServiceSpecBuilder().addNewPort().and()
        .withClusterIP("192.168.1.1").build();
    final Service service = new ServiceBuilder().withMetadata(build).withSpec(spec).build();
    server.expect().withPath("/api/v1/namespaces/default/services").andReturn(200, new ServiceListBuilder().addToItems().addToItems(service).build()).once();
    KubernetesClient client = this.client;
    final ServiceList list = client.services().inNamespace("default").list();
    assertNotNull(list);
    assertEquals("test", list.getItems().get(0).getMetadata().getLabels().get("test"));
    System.out.println(list.getItems().get(0).getSpec().getClusterIP());
  }

}
