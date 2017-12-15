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

package org.jacpfx.vxms.k8s.util;

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


/**
 * Created by amo on 13.04.17.
 */
public class ServiceUtil {

  private static final String SEPERATOR = ":";
  private static final Logger logger = Logger.getLogger(ServiceUtil.class.getName());

  public static void resolveK8SAnnotationsAndInit(
      Object bean,
      String user,
      String pwd,
      String api_token,
      String master_url,
      String namespace,
      KubernetesClient clientPassed) {
    final List<Field> serverNameFields = ServiceUtil.findServiceFields(bean);
    final List<Field> labelFields = ServiceUtil.findLabelields(bean);
    if (!serverNameFields.isEmpty()) {
      KubernetesClient client = getKubernetesClient(
          user,
          pwd,
          api_token,
          master_url,
          namespace,
          clientPassed);
      if (client != null) {
        ServiceUtil
            .findServiceEntryAndSetValue(bean, serverNameFields, client);
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
        ServiceUtil.findPodsAndEndpointsAndSetValue(bean, labelFields, client);
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

  public static void findServiceEntryAndSetValue(Object bean, List<Field> serverNameFields,
      KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(serviceNameField -> {
      final ServiceName serviceNameAnnotation = serviceNameField
          .getAnnotation(ServiceName.class);
      final String serviceName = serviceNameAnnotation.value();
      final boolean withLabel = serviceNameField.isAnnotationPresent(WithLabel.class);
      final boolean withLabels = serviceNameField.isAnnotationPresent(WithLabels.class);
      if (!withLabel && !withLabels) {
        resolveByServiceName(bean, client, serviceNameField, serviceName);
      } else {
        resolveByLabel(bean, client, serviceNameField, serviceName, withLabel, withLabels);


      }

    });
  }

  private static void resolveByLabel(Object bean, KubernetesClient client, Field serviceNameField,
      String serviceName, boolean withLabel, boolean withLabels) {
    if (serviceName != null && !serviceName.isEmpty()) {
      resolveByServicenameAndLabel(bean, client, serviceNameField, serviceName, withLabel,
          withLabels);
    } else {
      resoveByLabelOnly(bean, client, serviceNameField, withLabel, withLabels);
    }
  }

  private static void resolveByServicenameAndLabel(Object bean, KubernetesClient client,
      Field serviceNameField, String serviceName, boolean withLabel, boolean withLabels) {
    final List<Service> services = findServiceEntries(client, serviceName);
    final Map<String, String> labels = getLabelsFromAnnotation(serviceNameField, withLabel,
        withLabels);

    final List<Service> filteredServices = ServiceUtil.filterServicesByName(labels, services);
    if (filteredServices != null && !filteredServices.isEmpty()) {
      final Service serviceEntry = filteredServices.get(0);
      resolveHostAndSetValue(bean, serviceNameField, serviceEntry);
    }
  }

  private static void resoveByLabelOnly(Object bean, KubernetesClient client,
      Field serviceNameField, boolean withLabel, boolean withLabels) {
    final Map<String, String> labels = getLabelsFromAnnotation(serviceNameField, withLabel,
        withLabels);
    final Optional<ServiceList> serviceList = getServicesByLabel(labels, client);
    serviceList.ifPresent(list -> {
      if (!list.getItems().isEmpty()) {
        final Service serviceEntry = list.getItems().get(0);
        resolveHostAndSetValue(bean, serviceNameField, serviceEntry);
      }
    });
  }

  private static void resolveByServiceName(Object bean, KubernetesClient client,
      Field serviceNameField, String serviceName) {
    final Optional<Service> serviceEntryOptional = findServiceEntry(client, serviceName);
    serviceEntryOptional.ifPresent(serviceEntry -> resolveHostAndSetValue(bean, serviceNameField, serviceEntry));
  }

  private static void resolveHostAndSetValue(Object bean, Field serviceNameField,
      Service serviceEntry) {
    final String hostString = getHostString(serviceEntry);
    FieldUtil.setFieldValue(bean, serviceNameField, hostString);
  }

  private static Map<String, String> getLabelsFromAnnotation(Field serviceNameField,
      boolean withLabel, boolean withLabels) {
    final Map<String, String> labels = new HashMap<>();
    if (withLabel) {
      WithLabel wl = serviceNameField.getAnnotation(WithLabel.class);
      labels.put(wl.name(), wl.value());
    }
    if (withLabels) {
      WithLabels wl = serviceNameField.getAnnotation(WithLabels.class);
      Stream.of(wl.value()).forEach(wle -> labels.put(wle.name(), wle.value()));
    }
    return labels;
  }

  public static Optional<ServiceList> getServicesByLabel(Map<String, String> labels,
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

  public static List<Service> filterServicesByName(Map<String, String> labels,
      List<Service> services) {
    return services.stream().filter(service -> {
      final Map<String, String> additionalProperties = service.getMetadata().getLabels();
      final Set<Entry<String, String>> collect = additionalProperties.entrySet().stream()
          .filter(entry -> {
            if (!labels.containsKey(entry.getKey())) {
              return false;
            }
            return labels.get(entry.getKey()).equalsIgnoreCase(entry.getValue());
          }).collect(Collectors.toSet());
      return collect.size() == labels.size();
    }).collect(Collectors.toList());
  }

  public static void findPodsAndEndpointsAndSetValue(Object bean, List<Field> serverNameFields,
      KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(serviceNameField -> {
      // TODO handle WithLabels !!!
      final WithLabel serviceNameAnnotation = serviceNameField
          .getAnnotation(WithLabel.class);
      final String labelName = serviceNameAnnotation.name();
      final String labelValue = serviceNameAnnotation.value();

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

  public static List<Field> findServiceFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(ServiceName.class))
        .collect(Collectors.toList());
  }

  public static List<Field> findLabelields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(WithLabel.class))
        .collect(Collectors.toList());
  }


  public static Optional<Service> findServiceEntry(KubernetesClient client, String serviceName) {
    Objects.requireNonNull(client, "no client available");
    return Optional.ofNullable(client
        .services()
        .inNamespace(client.getNamespace())
        .list())
        .orElse(new ServiceList())
        .getItems()
        .stream()
        .filter(item -> {
          final Pattern glob = Pattern.compile(serviceName);
          return glob.matcher(item.getMetadata().getName()).find();
        })
        .findFirst();
  }

  public static List<Service> findServiceEntries(KubernetesClient client, String serviceName) {
    Objects.requireNonNull(client, "no client available");
    return Optional.ofNullable(client
        .services()
        .inNamespace(client.getNamespace())
        .list())
        .orElse(new ServiceList())
        .getItems()
        .stream()
        .filter(item -> {
          final Pattern glob = Pattern.compile(serviceName);
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
