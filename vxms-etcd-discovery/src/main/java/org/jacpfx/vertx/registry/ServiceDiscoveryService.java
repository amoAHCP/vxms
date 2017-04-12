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

package org.jacpfx.vertx.registry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import java.util.Optional;
import java.util.function.Consumer;
import or.jacpfx.spi.ServiceDiscoverySpi;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.vertx.etcd.client.CustomConnectionOptions;
import org.jacpfx.vertx.etcd.client.DefaultConnectionOptions;
import org.jacpfx.vertx.etcd.client.EtcdClient;

/**
 * Created by Andy Moncsek on 06.07.16.
 */
public class ServiceDiscoveryService implements ServiceDiscoverySpi {

  private EtcdRegistration etcdRegistration;

  public void registerService(Runnable onSuccess, Consumer<Throwable> onFail,
      AbstractVerticle verticleInstance) {
    etcdRegistration = createEtcdRegistrationHandler(verticleInstance);
    final Optional<EtcdRegistration> etcdRegistrationOpt = Optional
        .ofNullable(this.etcdRegistration);
    etcdRegistrationOpt.ifPresent(reg -> reg.connect(connection -> {
      if (connection.failed()) {
        onFail.accept(connection.cause());
      } else {
        onSuccess.run();
      }
    }));
    if (!etcdRegistrationOpt.isPresent()) {
      onSuccess.run();
    }
  }

  private EtcdRegistration createEtcdRegistrationHandler(AbstractVerticle verticleInstance) {
    if (verticleInstance == null || verticleInstance.config() == null) {
      return null;
    }
    int etcdPort;
    int ttl;
    String domain;
    String etcdHost;
    String serviceName;
    String serviceHostName;
    int servicePort;
    String contextRoot;
    HttpClientOptions clientOptions;
    CustomConnectionOptions connection;
    CustomServerOptions endpointConfig;
    HttpServerOptions options;
    if (verticleInstance.getClass().isAnnotationPresent(EtcdClient.class)) {
      final EtcdClient client = verticleInstance.getClass().getAnnotation(EtcdClient.class);
      etcdPort = ConfigurationUtil
          .getIntegerConfiguration(verticleInstance.config(), "etcdport", client.port());
      ttl = ConfigurationUtil
          .getIntegerConfiguration(verticleInstance.config(), "ttl", client.ttl());
      domain = ConfigurationUtil
          .getStringConfiguration(verticleInstance.config(), "domain", client.domain());
      etcdHost = ConfigurationUtil
          .getStringConfiguration(verticleInstance.config(), "etcdhost", client.host());
      connection = getConnectionOptions(client);
      clientOptions = connection.getClientOptions(verticleInstance.config());
      serviceHostName = ConfigurationUtil
          .getStringConfiguration(verticleInstance.config(), "exportedHost", client.exportedHost());
      servicePort = ConfigurationUtil
          .getIntegerConfiguration(verticleInstance.config(), "exportedPort",
              client.exportedPort());

      serviceName = ConfigurationUtil
          .getServiceName(verticleInstance.config(), verticleInstance.getClass());
      contextRoot = ConfigurationUtil
          .getContextRoot(verticleInstance.config(), verticleInstance.getClass());
      endpointConfig = ConfigurationUtil.getEndpointOptions(verticleInstance.getClass());
      options = endpointConfig.getServerOptions(verticleInstance.config());

    } else {
      etcdPort = verticleInstance.config().getInteger("etcdport", 0);
      ttl = verticleInstance.config().getInteger("ttl", 0);
      domain = verticleInstance.config().getString("domain", null);
      etcdHost = verticleInstance.config().getString("etcdhost", null);
      serviceHostName = verticleInstance.config().getString("exportedHost", "");
      servicePort = verticleInstance.config().getInteger("exportedPort", 0);
      contextRoot = ConfigurationUtil
          .getContextRoot(verticleInstance.config(), verticleInstance.getClass());
      serviceName = null;
      clientOptions = null;
      options = null;
    }

    if (domain == null || etcdHost == null || serviceName == null || etcdPort == 0) {
      return null;
    }

    return EtcdRegistration.
        buildRegistration().
        vertx(verticleInstance.getVertx()).
        clientOptions(clientOptions).
        etcdHost(etcdHost).
        etcdPort(etcdPort).
        ttl(ttl).
        domainName(domain).
        serviceName(serviceName).
        serviceHost(serviceHostName.isEmpty() ? getHostname(verticleInstance) : serviceHostName).
        servicePort(servicePort == 0 ? ConfigurationUtil
            .getEndpointPort(verticleInstance.config(), verticleInstance.getClass()) : servicePort).
        serviceContextRoot(contextRoot).
        secure(options.isSsl()).
        nodeName(verticleInstance.deploymentID());
  }

  private String getHostname(AbstractVerticle verticleInstance) {
    String host = System.getenv("COMPUTERNAME");
    if (host != null) {
      return host;
    }
    host = System.getenv("HOSTNAME");
    if (host != null) {
      return host;
    }
    return ConfigurationUtil
        .getEndpointHost(verticleInstance.config(), verticleInstance.getClass());
  }

  public void disconnect() {
    Optional.ofNullable(this.etcdRegistration).ifPresent(reg -> reg.disconnect(handler -> {

    }));
  }

  private CustomConnectionOptions getConnectionOptions(EtcdClient client) {
    try {
      return client.options().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return new DefaultConnectionOptions();
  }
}
