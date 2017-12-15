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
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jacpfx.vxms.common.util.ConfigurationUtil;
import org.jacpfx.vxms.k8s.util.FieldUtil;
import org.jacpfx.vxms.k8s.util.StringUtil;

public class KubeDiscovery {
  private static final String SEPERATOR = ":";

  /**
   * Resolves discovery annotation in AbstractVerticles
   * @param service the service where to resolve the annotations
   * @param kubeConfig the kubernetes config
   */
  public static void resolveBeanAnnotations(AbstractVerticle service, Config kubeConfig) {
    final JsonObject env = service.config();
    final List<Field> serviceNameFields = findServiceFields(service);
    final DefaultKubernetesClient client = new DefaultKubernetesClient(kubeConfig); // TODO be aware of OpenShiftClient
    if (!serviceNameFields.isEmpty()) {
      findServiceEntryAndSetValue(
          service, serviceNameFields, env, client);
    }
  }

  private static void findServiceEntryAndSetValue(
      Object bean, List<Field> serverNameFields, JsonObject env, KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(
        serviceNameField -> {
          final ServiceName serviceNameAnnotation =
              serviceNameField.getAnnotation(ServiceName.class);
          final String serviceName = serviceNameAnnotation.value();
          final boolean withLabel = serviceNameField.isAnnotationPresent(WithLabel.class);
          final boolean withLabels = serviceNameField.isAnnotationPresent(WithLabels.class);
          if (!withLabel && !withLabels) {
            resolveByServiceName(bean, env, client, serviceNameField, serviceName);
          } else {
            resoveByLabelOnly(bean, env, client, serviceNameField, withLabel, withLabels);
          }
        });
  }

  private static void resolveByServiceName(
      Object bean,
      JsonObject env,
      KubernetesClient client,
      Field serviceNameField,
      String serviceName) {
    final Optional<Service> serviceEntryOptional = findServiceEntry(env, client, serviceName);
    serviceEntryOptional.ifPresent(
        serviceEntry -> resolveHostAndSetValue(bean, serviceNameField, serviceEntry));
  }

  private static void resolveHostAndSetValue(
      Object bean, Field serviceNameField, Service serviceEntry) {
    final String hostString = getHostString(serviceEntry);
    FieldUtil.setFieldValue(bean, serviceNameField, hostString);
  }

  private static String getHostString(Service serviceEntry) {
    String hostString = "";
    final String clusterIP = serviceEntry.getSpec().getClusterIP();
    final List<ServicePort> ports = serviceEntry.getSpec().getPorts();
    if (ports.size()==1) { // only resolve port if unique
      String protocol =ports.get(0).getProtocol();
      if(StringUtil.isNullOrEmpty(protocol)) {
        hostString = clusterIP + SEPERATOR + ports.get(0).getPort();
      } else {
        hostString = protocol+"://"+clusterIP + SEPERATOR + ports.get(0).getPort();
      }

    }
    return hostString;
  }

  private static void resoveByLabelOnly(
      Object bean,
      JsonObject env,
      KubernetesClient client,
      Field serviceNameField,
      boolean withLabel,
      boolean withLabels) {
    final Map<String, String> labels =
        getLabelsFromAnnotation(env, serviceNameField, withLabel, withLabels);
    final Optional<ServiceList> serviceList = getServicesByLabel(labels, client);
    serviceList.ifPresent(
        list -> {
          if (!list.getItems().isEmpty()) {
            final Service serviceEntry = list.getItems().get(0);
            resolveHostAndSetValue(bean, serviceNameField, serviceEntry);
          }
        });
  }

  private static Map<String, String> getLabelsFromAnnotation(
      JsonObject env, Field serviceNameField, boolean withLabel, boolean withLabels) {
    final Map<String, String> labels = new HashMap<>();
    if (withLabel) {
      WithLabel wl = serviceNameField.getAnnotation(WithLabel.class);
      labels.put(resolveProperty(env, wl.name()), resolveProperty(env, wl.value()));
    }
    if (withLabels) {
      WithLabels wl = serviceNameField.getAnnotation(WithLabels.class);
      Stream.of(wl.value())
          .forEach(
              wle ->
                  labels.put(resolveProperty(env, wle.name()), resolveProperty(env, wle.value())));
    }
    return labels;
  }

  private static Optional<ServiceList> getServicesByLabel(
      Map<String, String> labels, KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    MixedOperation<Service, ServiceList, DoneableService, Resource<Service, DoneableService>>
        services = client.services();

    FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>> listable =
        null;
    for (Entry<String, String> entry : labels.entrySet()) {
      listable =
          listable == null
              ? services.withLabel(entry.getKey(), entry.getValue())
              : listable.withLabel(entry.getKey(), entry.getValue());
    }
    return Optional.ofNullable(listable.list());
  }

  private static List<Field> findServiceFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(ServiceName.class))
        .collect(Collectors.toList());
  }

  private static List<Field> findWithLabeFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(WithLabel.class))
        .collect(Collectors.toList());
  }

  private static List<Field> findWithLabesFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(WithLabels.class))
        .collect(Collectors.toList());
  }

  private static Optional<Service> findServiceEntry(
      JsonObject env, KubernetesClient client, String serviceName) {
    Objects.requireNonNull(client, "no client available");
    final String resolvedServiceName = resolveProperty(env, serviceName);
    return Optional.ofNullable(client.services().inNamespace(client.getNamespace()).list())
        .orElse(new ServiceList())
        .getItems()
        .stream()
        .filter(item -> item.getMetadata().getName().equalsIgnoreCase(resolvedServiceName))
        .findFirst();
  }

  private static String resolveProperty(JsonObject env, String serviceName) {
    String result = serviceName;
    if (serviceName.contains("$")) {
      result = serviceName.substring(2, serviceName.length() - 1);
      result = ConfigurationUtil.getStringConfiguration(env, result, result);
    }
    return result;
  }
}
