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

import io.fabric8.annotations.ServiceName;
import io.fabric8.annotations.WithLabel;
import io.fabric8.annotations.WithLabels;
import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jacpfx.vxms.k8s.discovery.Endpoints;
import org.jacpfx.vxms.k8s.discovery.Pods;
import org.jacpfx.vxms.k8s.util.FieldUtil;
import org.jacpfx.vxms.k8s.util.KubeClientBuilder;
@Deprecated
public class DiscoveryUtil {

  private static final String SEPERATOR = ":";
  private static final Logger logger = Logger.getLogger(DiscoveryUtil.class.getName());

  /**
   * Resolves all Kubernetes discovery annotation on a Spring bean
   *
   * @param bean The bean containing the Annotations
   * @param user The Kubernetes master user
   * @param pwd The Kubernetes master password
   * @param api_token The Kubernetes api Token
   * @param master_url The Kubernetes master url
   * @param namespace The Kubernetes namespace to do discovery
   * @param env The Spring environmental handler
   * @param clientPassed A externally (optional) passed Kubernetes client
   */
  public static void resolveK8SAnnotationsAndInit(
      Object bean,
      String user,
      String pwd,
      String api_token,
      String master_url,
      String namespace,
      JsonObject env,
      KubernetesClient clientPassed) {
    final List<Field> serverNameFields = DiscoveryUtil.findServiceFields(bean);
    final List<Field> labelFields = DiscoveryUtil.findLabelields(bean);
    if (!serverNameFields.isEmpty()) {
      KubernetesClient client = getKubernetesClient(
          user,
          pwd,
          api_token,
          master_url,
          namespace,
          clientPassed);
      if (client != null) {
        DiscoveryUtil
            .findServiceEntryAndSetValue(bean, serverNameFields, env, client);
      } else {
        logger.info("no Kubernetes client available");
      }

    }

    if (!labelFields.isEmpty()) {
      KubernetesClient client = getKubernetesClient(
          user,
          pwd,
          api_token,
          master_url,
          namespace,
          clientPassed);
      if (client != null) {
        DiscoveryUtil.findPodsAndEndpointsAndSetValue(bean, labelFields, env, client);
      } else {
        logger.info("no Kubernetes client available");
      }

    }
  }

  private static KubernetesClient getKubernetesClient(String user, String pwd, String api_token,
      String master_url, String namespace, KubernetesClient clientPassed) {
    return clientPassed != null ? clientPassed
        : KubeClientBuilder.buildKubernetesClient(user, pwd, api_token, master_url, namespace);
  }

  private static void findServiceEntryAndSetValue(Object bean, List<Field> serverNameFields,
      JsonObject env, KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(serviceNameField -> {
      final ServiceName serviceNameAnnotation = serviceNameField
          .getAnnotation(ServiceName.class);
      final String serviceName = serviceNameAnnotation.value();
      final boolean withLabel = serviceNameField.isAnnotationPresent(WithLabel.class);
      final boolean withLabels = serviceNameField.isAnnotationPresent(WithLabels.class);
      if (!withLabel && !withLabels) {
        resolveByServiceName(bean, env, client, serviceNameField, serviceName);
      } else {
        resolveByLabel(bean, env, client, serviceNameField, serviceName, withLabel, withLabels);


      }

    });
  }

  private static void resolveByLabel(Object bean, JsonObject env, KubernetesClient client,
      Field serviceNameField,
      String serviceName, boolean withLabel, boolean withLabels) {
    if (serviceName != null && !serviceName.isEmpty()) {
      resolveByServicenameAndLabel(bean, env, client, serviceNameField, serviceName, withLabel,
          withLabels);
    } else {
      resoveByLabelOnly(bean, env, client, serviceNameField, withLabel, withLabels);
    }
  }

  private static void resolveByServicenameAndLabel(Object bean, JsonObject env,
      KubernetesClient client,
      Field serviceNameField, String serviceName, boolean withLabel, boolean withLabels) {
    final List<Service> services = findServiceEntries(env, client, serviceName);
    final Map<String, String> labels = getLabelsFromAnnotation(env, serviceNameField, withLabel,
        withLabels);

    final List<Service> filteredServices = DiscoveryUtil.filterServicesByName(labels, services);
    if (filteredServices != null && !filteredServices.isEmpty()) {
      final Service serviceEntry = filteredServices.get(0);
      resolveHostAndSetValue(bean, serviceNameField, serviceEntry);
    }
  }

  private static void resoveByLabelOnly(Object bean, JsonObject env, KubernetesClient client,
      Field serviceNameField, boolean withLabel, boolean withLabels) {
    final Map<String, String> labels = getLabelsFromAnnotation(env, serviceNameField, withLabel,
        withLabels);
    final Optional<ServiceList> serviceList = getServicesByLabel(labels, client);
    serviceList.ifPresent(list -> {
      if (!list.getItems().isEmpty()) {
        final Service serviceEntry = list.getItems().get(0);
        resolveHostAndSetValue(bean, serviceNameField, serviceEntry);
      }
    });
  }

  private static void resolveByServiceName(Object bean, JsonObject env, KubernetesClient client,
      Field serviceNameField, String serviceName) {
    final Optional<Service> serviceEntryOptional = findServiceEntry(env, client, serviceName);
    serviceEntryOptional
        .ifPresent(serviceEntry -> resolveHostAndSetValue(bean, serviceNameField, serviceEntry));
  }

  private static void resolveHostAndSetValue(Object bean, Field serviceNameField,
      Service serviceEntry) {
    final String hostString = getHostString(serviceEntry);
    FieldUtil.setFieldValue(bean, serviceNameField, hostString);
  }

  private static Map<String, String> getLabelsFromAnnotation(JsonObject env,
      Field serviceNameField,
      boolean withLabel, boolean withLabels) {
    final Map<String, String> labels = new HashMap<>();
    if (withLabel) {
      WithLabel wl = serviceNameField.getAnnotation(WithLabel.class);
      labels.put(resolveProperty(env, wl.name()), resolveProperty(env, wl.value()));
    }
    if (withLabels) {
      WithLabels wl = serviceNameField.getAnnotation(WithLabels.class);
      Stream.of(wl.value()).forEach(
          wle -> labels.put(resolveProperty(env, wle.name()), resolveProperty(env, wle.value())));
    }
    return labels;
  }

  private static Optional<ServiceList> getServicesByLabel(Map<String, String> labels,
      KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    MixedOperation<Service, ServiceList, DoneableService, Resource<Service, DoneableService>> services = client
        .services();

    FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>> listable = null;
    for (Entry<String, String> entry : labels.entrySet()) {
      listable = listable == null ? services
          .withLabel(entry.getKey(), entry.getValue())
          : listable.withLabel(entry.getKey(), entry.getValue());

    }
    return Optional.ofNullable(listable
        .list());
  }

  private static List<Service> filterServicesByName(Map<String, String> labels,
      List<Service> services) {
    return services.stream().filter(service -> {
      final Map<String, String> additionalProperties = service.getMetadata().getLabels();
      final Set<Entry<String, String>> collect = additionalProperties.entrySet().stream()
          .filter(entry -> labels.containsKey(entry.getKey()) && labels.get(entry.getKey())
              .equalsIgnoreCase(entry.getValue())).collect(Collectors.toSet());
      return collect.size() == labels.size();
    }).collect(Collectors.toList());
  }

  private static void findPodsAndEndpointsAndSetValue(Object bean, List<Field> serverNameFields,
      JsonObject env, KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(serviceNameField -> {
      // TODO handle WithLabels !!!
      final WithLabel serviceNameAnnotation = serviceNameField
          .getAnnotation(WithLabel.class);
      final String labelName = resolveProperty(env, serviceNameAnnotation.name());
      final String labelValue = resolveProperty(env, serviceNameAnnotation.value());

      setEndpoints(bean, client, serviceNameField, labelName, labelValue);

      setPods(bean, client, serviceNameField, labelName, labelValue);

    });
  }

  private static void setPods(Object bean, KubernetesClient client,
      Field serviceNameField, String labelName, String labelValue) {
    Objects.requireNonNull(client, "no client available");
    if (serviceNameField.getType().isAssignableFrom(Pods.class)) {
      Pods pods = Pods.build().client(client).namespace(client.getNamespace())
          .labelName(labelName).labelValue(labelValue);
      FieldUtil.setFieldValue(bean, serviceNameField, pods);
    }
  }

  private static void setEndpoints(Object bean, KubernetesClient client,
      Field serviceNameField, String labelName, String labelValue) {
    Objects.requireNonNull(client, "no client available");
    if (serviceNameField.getType().isAssignableFrom(Endpoints.class)) {
      Endpoints endpoint = Endpoints.build().client(client).namespace(client.getNamespace())
          .labelName(labelName).labelValue(labelValue);
      FieldUtil.setFieldValue(bean, serviceNameField, endpoint);
    }
  }

  private static List<Field> findServiceFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(ServiceName.class))
        .collect(Collectors.toList());
  }

  private static List<Field> findLabelields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(WithLabel.class))
        .collect(Collectors.toList());
  }


  private static Optional<Service> findServiceEntry(JsonObject env, KubernetesClient client,
      String serviceName) {
    Objects.requireNonNull(client, "no client available");
    final String resolvedServiceName = resolveProperty(env, serviceName);
    return Optional.ofNullable(client
        .services()
        .inNamespace(client.getNamespace())
        .list())
        .orElse(new ServiceList())
        .getItems()
        .stream()
        .filter(item -> {
          final Pattern glob = Pattern.compile(resolvedServiceName);
          return glob.matcher(item.getMetadata().getName()).find();
        })
        .findFirst();
  }

  private static String resolveProperty(JsonObject env, String serviceName) {
    String result = serviceName;
    if (serviceName.contains("$")) {
      result = serviceName.substring(2, serviceName.length() - 1);
      result = env.getString(result);
    }
    return result;
  }

  private static List<Service> findServiceEntries(JsonObject env, KubernetesClient client,
      String serviceName) {
    Objects.requireNonNull(client, "no client available");
    final String resolvedServiceName = resolveProperty(env, serviceName);
    return Optional.ofNullable(client
        .services()
        .inNamespace(client.getNamespace())
        .list())
        .orElse(new ServiceList())
        .getItems()
        .stream()
        .filter(item -> {
          final Pattern glob = Pattern.compile(resolvedServiceName);
          return glob.matcher(item.getMetadata().getName()).find();
        }).collect(Collectors.toList());
  }


  private static String getHostString(Service serviceEntry) {
    String hostString = "";
    final String clusterIP = serviceEntry.getSpec().getClusterIP();
    final List<ServicePort> ports = serviceEntry.getSpec().getPorts();
    if (!ports.isEmpty()) {
      hostString = clusterIP + SEPERATOR + ports.get(0).getPort();
    }
    return hostString;
  }
}
