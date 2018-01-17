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

import io.fabric8.annotations.PortName;
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
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
   *
   * @param service the service where to resolve the annotations
   * @param kubeConfig the kubernetes config
   */
  public static void resolveBeanAnnotations(AbstractVerticle service, Config kubeConfig) {
    final JsonObject env = service.config();
    final List<Field> serviceNameFields = findServiceFields(service);
    if (!env.getBoolean("kube.offline", false)) { // online
      final DefaultKubernetesClient client =
          new DefaultKubernetesClient(kubeConfig); // TODO be aware of OpenShiftClient
      if (!serviceNameFields.isEmpty()) {
        findServiceEntryAndSetValue(service, serviceNameFields, env, client);
      } else {
        // TODO check and handle Endpoints & Pods
      }
    } else {
      // resolve properties offline
      if (!serviceNameFields.isEmpty()) {
        resolveServicesOffline(service, serviceNameFields, env);
      } else {
        // TODO check and handle Endpoints & Pods
      }
    }
  }

  private static void resolveServicesOffline(
      Object bean, List<Field> serverNameFields, JsonObject env) throws KubernetesClientException {
    serverNameFields.forEach(
        serviceNameField -> {
          final ServiceName serviceNameAnnotation =
              serviceNameField.getAnnotation(ServiceName.class);
          final String serviceName = serviceNameAnnotation.value();
          final boolean withLabel = serviceNameField.isAnnotationPresent(WithLabel.class);
          final boolean withLabels = serviceNameField.isAnnotationPresent(WithLabels.class);
          if (isServiceNameOnly(withLabel, withLabels)) {
            resolveOfflineByServiceName(bean, env, serviceNameField, serviceName);
          } else {
            resolveOfflineServiceByLabelOnly(bean, env, serviceNameField, withLabel, withLabels);
          }
        });
  }

  private static void findServiceEntryAndSetValue(
      Object bean, List<Field> serverNameFields, JsonObject env, KubernetesClient client)
      throws KubernetesClientException {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(
        serviceNameField -> {
          final ServiceName serviceNameAnnotation =
              serviceNameField.getAnnotation(ServiceName.class);
          final String serviceName = serviceNameAnnotation.value();
          final boolean withLabel = serviceNameField.isAnnotationPresent(WithLabel.class);
          final boolean withLabels = serviceNameField.isAnnotationPresent(WithLabels.class);
          if (isServiceNameOnly(withLabel, withLabels)) {
            resolveByServiceName(bean, env, client, serviceNameField, serviceName);
          } else {
            resolveServiceByLabelOnly(bean, env, client, serviceNameField, withLabel, withLabels);
          }
        });
  }

  private static boolean isServiceNameOnly(boolean withLabel, boolean withLabels) {
    return !withLabel && !withLabels;
  }

  private static void resolveOfflineByServiceName(
      Object bean, JsonObject env, Field serviceNameField, String serviceName) {
    final Optional<String> serviceEntryOptional = findOfflineServiceEntry(env, serviceName);
    serviceEntryOptional.ifPresent(
        serviceEntry -> resolveOfflibneHostAndSetValue(bean, env, serviceNameField, serviceEntry));
  }

  private static void resolveOfflineServiceByLabelOnly(
      Object bean, JsonObject env, Field serviceNameField, boolean withLabel, boolean withLabels) {

    final Map<String, String> labelsFromAnnotation =
        getLabelsFromAnnotation(env, serviceNameField, withLabel, withLabels);

    final Optional<String> localAccessKey =
        labelsFromAnnotation
            .entrySet()
            .stream()
            .map(entry -> entry.getKey().concat(".").concat(entry.getValue()))
            .collect(
                ArrayList<String>::new,
                (list, e) -> list.add(0, e),
                (list1, list2) -> list1.addAll(0, list2))
            .stream()
            .reduce(
                (a, b) -> {
                  String val = "";
                  if (b != null) {
                    val += a.concat(".").concat(b);
                  } else {
                    val += a;
                  }
                  return val;
                });

    localAccessKey.ifPresent(
        accessKey -> {
          String serviceValue = ConfigurationUtil.getStringConfiguration(env, accessKey, accessKey);
          String hostString = getOfflineHostString(serviceValue, env, serviceNameField);
          FieldUtil.setFieldValue(bean, serviceNameField, hostString);
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
        serviceEntry -> resolveHostAndSetValue(bean, env, serviceNameField, serviceEntry));
  }

  private static void resolveHostAndSetValue(
      Object bean, JsonObject env, Field serviceNameField, Service serviceEntry) {
    final String hostString = getHostString(serviceEntry, env, serviceNameField);
    FieldUtil.setFieldValue(bean, serviceNameField, hostString);
  }

  private static void resolveOfflibneHostAndSetValue(
      Object bean, JsonObject env, Field serviceNameField, String serviceEntry) {
    final String hostString = getOfflineHostString(serviceEntry, env, serviceNameField);
    FieldUtil.setFieldValue(bean, serviceNameField, hostString);
  }

  private static String getHostString(
      Service serviceEntry, JsonObject env, Field serviceNameField) {
    String hostString = "";
    final String clusterIP = serviceEntry.getSpec().getClusterIP();
    final List<ServicePort> ports = serviceEntry.getSpec().getPorts();
    if (serviceNameField.isAnnotationPresent(PortName.class)) {
      hostString = resolveServiceWithPortName(env, serviceNameField, clusterIP, ports);
    } else {
      hostString = resolveService(hostString, clusterIP, ports);
    }
    return hostString;
  }

  private static String getOfflineHostString(
      String serviceEntry, JsonObject env, Field serviceNameField) {
    String hostString = "";
    if (serviceNameField.isAnnotationPresent(PortName.class)) {
      hostString = resolveOfflineServiceWithPortName(env, serviceNameField, serviceEntry);
    } else {
      hostString = serviceEntry;
    }
    return hostString;
  }

  private static String resolveService(
      String hostString, String clusterIP, List<ServicePort> ports) {
    if (ports.size() >= 1) {
      final ServicePort servicePort = ports.get(0);
      final String protocol = servicePort.getProtocol();
      hostString = buildServiceHostString(clusterIP, servicePort, protocol);
    }
    return hostString;
  }

  private static String resolveServiceWithPortName(
      JsonObject env, Field serviceNameField, String clusterIP, List<ServicePort> ports) {
    String hostString = null;
    final PortName portNameAnnotation = serviceNameField.getAnnotation(PortName.class);
    final String portName = resolveProperty(env, portNameAnnotation.value());
    final Optional<ServicePort> portMatch = findPortByName(ports, portName);
    if (portMatch.isPresent()) {
      final ServicePort port = portMatch.get();
      final String protocol = port.getProtocol();
      hostString = buildServiceHostString(clusterIP, port, protocol);
    }
    return hostString;
  }

  private static String resolveOfflineServiceWithPortName(
      JsonObject env, Field serviceNameField, String serviceEntry) {
    final PortName portNameAnnotation = serviceNameField.getAnnotation(PortName.class);
    final String portName = resolveProperty(env, portNameAnnotation.value());
    final String portValue = ConfigurationUtil.getStringConfiguration(env, portName, null);
    return buildOfflineServiceHostString(serviceEntry, portValue, null);
  }

  private static Optional<ServicePort> findPortByName(List<ServicePort> ports, String portName) {
    return ports.stream().filter(port -> port.getName().equalsIgnoreCase(portName)).findFirst();
  }

  private static String buildOfflineServiceHostString(
      String clusterIP, String port, String protocol) {
    String hostString;
    if (StringUtil.isNullOrEmpty(protocol)) {
      hostString = clusterIP + SEPERATOR + port;
    } else {
      hostString = protocol + "://" + clusterIP + SEPERATOR + port;
    }
    return hostString;
  }

  private static String buildServiceHostString(
      String clusterIP, ServicePort port, String protocol) {
    String hostString;
    if (StringUtil.isNullOrEmpty(protocol)) {
      hostString = clusterIP + SEPERATOR + port.getPort();
    } else {
      hostString = protocol + "://" + clusterIP + SEPERATOR + port.getPort();
    }
    return hostString;
  }

  private static void resolveServiceByLabelOnly(
      Object bean,
      JsonObject env,
      KubernetesClient client,
      Field serviceNameField,
      boolean withLabel,
      boolean withLabels)
      throws KubernetesClientException {
    final Map<String, String> labels =
        getLabelsFromAnnotation(env, serviceNameField, withLabel, withLabels);
    final ServiceList serviceListResult = getServicesByLabel(labels, client);
    Optional.ofNullable(serviceListResult)
        .ifPresent(
            list -> {
              if (!list.getItems().isEmpty() && list.getItems().size() == 1) {
                final Service serviceEntry = list.getItems().get(0);
                resolveHostAndSetValue(bean, env, serviceNameField, serviceEntry);
              } else if (!list.getItems().isEmpty() && list.getItems().size() > 1) {
                handleNonUniqueLabelsError(labels);
              }
            });
  }

  private static void handleNonUniqueLabelsError(Map<String, String> labels) {
    final String entries =
        labels
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue() + " ")
            .reduce((a, b) -> a + b)
            .get();
    throw new KubernetesClientException("labels " + entries + " returns a non unique result");
  }

  private static Map<String, String> getLabelsFromAnnotation(
      JsonObject env, Field serviceNameField, boolean withLabel, boolean withLabels) {
    final Map<String, String> labels = new HashMap<>();
    if (withLabel) {
      final WithLabel wl = serviceNameField.getAnnotation(WithLabel.class);
      labels.put(resolveProperty(env, wl.name()), resolveProperty(env, wl.value()));
    }
    if (withLabels) {
      final WithLabels wls = serviceNameField.getAnnotation(WithLabels.class);
      labels.putAll(
          Stream.of(wls.value())
              .collect(
                  Collectors.toMap(
                      wl -> resolveProperty(env, wl.name()),
                      wl -> resolveProperty(env, wl.value()))));
    }
    return labels;
  }

  private static ServiceList getServicesByLabel(Map<String, String> labels, KubernetesClient client)
      throws KubernetesClientException {
    Objects.requireNonNull(client, "no client available");
    final MixedOperation<Service, ServiceList, DoneableService, Resource<Service, DoneableService>>
        services = client.services();
    final FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>>
        listable = createLabelFilterQuery(labels, services);
    return listable.list();
  }

  private static FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>>
      createLabelFilterQuery(
          Map<String, String> labels,
          MixedOperation<Service, ServiceList, DoneableService, Resource<Service, DoneableService>>
              services) {
    FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>> listable =
        null;
    for (Entry<String, String> entry : labels.entrySet()) {
      listable =
          listable == null
              ? services.withLabel(entry.getKey(), entry.getValue())
              : listable.withLabel(entry.getKey(), entry.getValue());
    }
    return listable;
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

  private static Optional<String> findOfflineServiceEntry(JsonObject env, String serviceName) {
    final String resolvedServiceName = resolveProperty(env, serviceName);
    final String resolvedValue =
        ConfigurationUtil.getStringConfiguration(env, resolvedServiceName, resolvedServiceName);
    return Optional.ofNullable(resolvedValue);
  }

  private static String resolveProperty(JsonObject env, String serviceName) {
    String result = serviceName;
    if (serviceName.contains("$")) {
      result = serviceName.substring(2, serviceName.length() - 1); // scheme: ${value}
      result = ConfigurationUtil.getStringConfiguration(env, result, result);
    }
    return result;
  }
}
