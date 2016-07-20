package org.jacpfx.vertx.registry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import or.jacpfx.spi.ServiceDiscoverySpi;
import org.jacpfx.common.util.ConfigurationUtil;
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
       if(!etcdRegistrationOpt.isPresent()) onSuccess.run();
    }
    // TODO add HttpClientOptions see:DiscoveryClientBuilder
    private EtcdRegistration createEtcdRegistrationHandler(AbstractVerticle verticleInstance) {
        if(verticleInstance==null || verticleInstance.config()==null) return null;
        int etcdPort;
        int ttl;
        String domain;
        String etcdHost;
        String serviceName;
        if(verticleInstance.getClass().isAnnotationPresent(EtcdClient.class)) {
            final EtcdClient client = verticleInstance.getClass().getAnnotation(EtcdClient.class);
            etcdPort = verticleInstance.config().getInteger("etcdport", client.port());
            ttl = verticleInstance.config().getInteger("ttl", client.ttl());
            domain = verticleInstance.config().getString("domain", client.domain());
            etcdHost = verticleInstance.config().getString("etcdhost", client.host());
            serviceName = ConfigurationUtil.serviceName(verticleInstance.config(), verticleInstance.getClass());
        } else {
            etcdPort = verticleInstance.config().getInteger("etcdport",0);
            ttl = verticleInstance.config().getInteger("ttl",0);
            domain = verticleInstance.config().getString("domain",null);
            etcdHost = verticleInstance.config().getString("etcdhost",null);
            serviceName = ConfigurationUtil.serviceName(verticleInstance.config(), verticleInstance.getClass());
        }

        if(domain==null || etcdHost==null || serviceName==null || etcdPort==0) return null;

        return EtcdRegistration.
                buildRegistration().
                vertx(Vertx.vertx()).
                etcdHost(etcdHost).
                etcdPort(etcdPort).
                ttl(ttl).
                domainName(domain).
                serviceName(serviceName).
                serviceHost(ConfigurationUtil.getEndpointHost(verticleInstance.config(), verticleInstance.getClass())).
                servicePort(ConfigurationUtil.getEndpointPort(verticleInstance.config(), verticleInstance.getClass())).
                nodeName(verticleInstance.deploymentID());
    }

    public void disconnect() {
        Optional.ofNullable(this.etcdRegistration).ifPresent(reg -> reg.disconnect(handler -> {

        }));
    }
}
