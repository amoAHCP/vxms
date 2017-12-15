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

package org.jacpfx.vxms.k8s.discovery;

import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Created by amo on 13.04.17.
 */
public class Endpoints {

  private final KubernetesClient client;
  private final String namespace, labelName, labelValue;

  private Endpoints(KubernetesClient client, String namespace, String labelName,
      String labelValue) {
    this.client = client;
    this.namespace = namespace;
    this.labelName = labelName;
    this.labelValue = labelValue;
  }

  public interface LabelValue {

    Endpoints labelValue(String labelValue);
  }

  public interface LabelName {

    LabelValue labelName(String serviceName);
  }

  public interface Namespace {

    LabelName namespace(String namespace);
  }

  public interface Client {

    Namespace client(KubernetesClient client);
  }

  public static Client build() {
    return client -> namespace -> labelName -> labelValue -> new Endpoints(client, namespace,
        labelName, labelValue);
  }

  public EndpointsList getEndpoints() {
    return labelValue != null && !labelValue.isEmpty() ?
        client.endpoints().inNamespace(namespace).withLabel(labelName, labelValue).list() :
        client.endpoints().inNamespace(namespace).withLabel(labelName).list();
  }

}
