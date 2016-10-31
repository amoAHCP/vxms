package org.jacpfx.vertx.registry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import or.jacpfx.spi.ServiceDiscoverySpi;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.vertx.etcd.client.CustomConnectionOptions;
import org.jacpfx.vertx.etcd.client.DefaultConnectionOptions;
import org.jacpfx.vertx.etcd.client.EtcdClient;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 06.07.16.
 */
public class ServiceDiscoveryService implements ServiceDiscoverySpi {
    private EtcdRegistration etcdRegistration;

    public void registerService(Runnable onSuccess, Consumer<Throwable> onFail, AbstractVerticle verticleInstance) {
        etcdRegistration = createEtcdRegistrationHandler(verticleInstance);
        final Optional<EtcdRegistration> etcdRegistrationOpt = Optional.ofNullable(this.etcdRegistration);
        etcdRegistrationOpt.ifPresent(reg -> reg.connect(connection -> {
            if (connection.failed()) {
                onFail.accept(connection.cause());
            } else {
                onSuccess.run();
            }
        }));
        if (!etcdRegistrationOpt.isPresent()) onSuccess.run();
    }

    private EtcdRegistration createEtcdRegistrationHandler(AbstractVerticle verticleInstance) {
        if (verticleInstance == null || verticleInstance.config() == null) return null;
        int etcdPort;
        int ttl;
        String domain;
        String etcdHost;
        String serviceName;
        String contextRoot;
        HttpClientOptions clientOptions;
        CustomConnectionOptions connection;
        CustomServerOptions endpointConfig;
        HttpServerOptions options;
        if (verticleInstance.getClass().isAnnotationPresent(EtcdClient.class)) {
            final EtcdClient client = verticleInstance.getClass().getAnnotation(EtcdClient.class);
            etcdPort = verticleInstance.config().getInteger("etcdport", client.port());
            ttl = verticleInstance.config().getInteger("ttl", client.ttl());
            domain = verticleInstance.config().getString("domain", client.domain());
            etcdHost = verticleInstance.config().getString("etcdhost", client.host());
            connection = getConnectionOptions(client);
            clientOptions = connection.getClientOptions(verticleInstance.config());


            serviceName = ConfigurationUtil.getServiceName(verticleInstance.config(), verticleInstance.getClass());
            contextRoot = ConfigurationUtil.getContextRoot(verticleInstance.config(), verticleInstance.getClass());
            endpointConfig = ConfigurationUtil.getEndpointOptions(verticleInstance.getClass());
            options = endpointConfig.getServerOptions(verticleInstance.config());

        } else {
            etcdPort = verticleInstance.config().getInteger("etcdport", 0);
            ttl = verticleInstance.config().getInteger("ttl", 0);
            domain = verticleInstance.config().getString("domain", null);
            etcdHost = verticleInstance.config().getString("etcdhost", null);
            contextRoot = ConfigurationUtil.getContextRoot(verticleInstance.config(), verticleInstance.getClass());
            serviceName = null;
            clientOptions = null;
            options = null;
        }

        if (domain == null || etcdHost == null || serviceName == null || etcdPort == 0) return null;

        return EtcdRegistration.
                buildRegistration().
                vertx(verticleInstance.getVertx()).
                clientOptions(clientOptions).
                etcdHost(etcdHost).
                etcdPort(etcdPort).
                ttl(ttl).
                domainName(domain).
                serviceName(serviceName).
                serviceHost(getHostname(verticleInstance)).
                servicePort(ConfigurationUtil.getEndpointPort(verticleInstance.config(), verticleInstance.getClass())).
                serviceContextRoot(contextRoot).
                secure(options.isSsl()).
                nodeName(verticleInstance.deploymentID());
    }

    private String getHostname(AbstractVerticle verticleInstance) {
        String host = System.getenv("COMPUTERNAME");
        if (host != null)
            return host;
        host = System.getenv("HOSTNAME");
        if (host != null)
            return host;
        return ConfigurationUtil.getEndpointHost(verticleInstance.config(), verticleInstance.getClass());
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
