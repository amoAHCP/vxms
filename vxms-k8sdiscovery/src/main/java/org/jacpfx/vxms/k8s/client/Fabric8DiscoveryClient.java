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

package org.jacpfx.vxms.k8s.client;

import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.jacpfx.vxms.k8s.util.KubeClientBuilder;
import org.jacpfx.vxms.k8s.util.ServiceUtil;

/**
 * Created by amo on 06.04.17.
 */
@Deprecated
public class Fabric8DiscoveryClient {

  private final String user, pwd, api_token, master_url, namespace;
  private final KubernetesClient client;
  private final Logger logger = Logger.getLogger(Fabric8DiscoveryClient.class.getName());

  public Fabric8DiscoveryClient() {
    user = null;
    pwd = null;
    api_token = null;
    master_url = null;
    namespace = null;
    client = null;
  }

  private Fabric8DiscoveryClient(
      String user,
      String pwd,
      String api_token,
      String master_url,
      String namespace) {
    this.user = user;
    this.pwd = pwd;
    this.api_token = api_token;
    this.namespace = namespace;
    this.master_url = master_url;
    this.client = KubeClientBuilder.buildKubernetesClient(
        this.user,
        this.pwd,
        this.api_token,
        this.master_url,
        this.namespace);
  }


  public interface User {

    Pwd user(String user);
  }

  public interface Pwd {

    ApiToken pwd(String pwd);
  }

  public interface ApiToken {

    MasterUrl apiToken(String token);
  }

  public interface MasterUrl {

    Namespace masterUrl(String masterUrl);
  }

  public interface Namespace {

    Fabric8DiscoveryClient namespace(String namespace);
  }

  public static User builder() {
    return user -> pwd -> apitoken -> masterurl -> namespace -> new Fabric8DiscoveryClient(
        user,
        pwd,
        apitoken,
        masterurl,
        namespace);
  }

  public void findServiceByName(String serviceName, Consumer<Service> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<Service> serviceEntryOptional = ServiceUtil
        .findServiceEntry(client, serviceName);
    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with name " + serviceName + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);

  }

  public void findServicesByLabel(String label, Consumer<ServiceList> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<ServiceList> serviceEntryOptional = Optional.ofNullable(client
        .services()
        .withLabel(label)
        .list());

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);
  }

  public void findServicesByLabel(String label, String value, Consumer<ServiceList> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<ServiceList> serviceEntryOptional = Optional.ofNullable(client
        .services()
        .withLabel(label, value)
        .list());

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);
  }

  public void findServicesByLabel(Map<String, String> labels, Consumer<ServiceList> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<ServiceList> serviceEntryOptional = ServiceUtil.getServicesByLabel(labels,client);

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + labels + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);
  }



  /**
   * Uses a regex for Services
   */
  public void findServicesByNameAndLabel(String name, Map<String, String> labels,
      Consumer<List<Service>> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final List<Service> services = ServiceUtil.findServiceEntries(client, name);

    final List<Service> filteredServices = ServiceUtil.filterServicesByName(labels, services);
    if (filteredServices.isEmpty()) {
      error.accept(
          new Throwable("no service with label " + labels + " and name:" + name + " found"));
    } else {
      serviceConsumer.accept(filteredServices);
    }

  }



  public void findEndpointsByLabel(String label, Consumer<EndpointsList> endpointsListConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<EndpointsList> serviceEntryOptional = Optional.ofNullable(client
        .endpoints()
        .withLabel(label)
        .list());

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    serviceEntryOptional.ifPresent(endpointsListConsumer::accept);
  }

  public void findEndpointsByLabel(String label, String value,
      Consumer<EndpointsList> endpointsListConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<EndpointsList> serviceEntryOptional = Optional.ofNullable(client
        .endpoints()
        .withLabel(label, value)
        .list());

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    serviceEntryOptional.ifPresent(endpointsListConsumer::accept);
  }


  public void resolveAnnotations(Object bean) {
    Objects.requireNonNull(client, "no client available");
    final List<Field> serverNameFields = ServiceUtil.findServiceFields(bean);
    final List<Field> labelFields = ServiceUtil.findLabelields(bean);
    if (!serverNameFields.isEmpty()) {
      ServiceUtil.findServiceEntryAndSetValue(bean, serverNameFields, client);
    }

    if (!labelFields.isEmpty()) {
      ServiceUtil.findPodsAndEndpointsAndSetValue(bean, labelFields, client);
    }

  }

}
