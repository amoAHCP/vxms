package org.jacpfx.vertx.etcd.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import or.jacpfx.spi.DiscoveryClientSpi;

import java.net.URI;
import java.util.MissingResourceException;
import java.util.Objects;

/**
 * Created by Andy Moncsek on 23.06.16.
 */
public class DiscoveryClientBuilder  implements DiscoveryClientSpi<DiscoveryClientEtcd>{
    public static final String ETCD_BASE_PATH = "/v2/keys/";
    @Override
    public DiscoveryClientEtcd getClient(AbstractVerticle verticleInstance) {
        if(verticleInstance.getClass().isAnnotationPresent(EtcdClient.class)){
            final EtcdClient client = verticleInstance.getClass().getAnnotation(EtcdClient.class);
            final URI fetchAll = URI.create("http://" + client.host() + ":" + client.port() + ETCD_BASE_PATH + client.domain() + "/?recursive=true");
            return new DiscoveryClientEtcd(verticleInstance.getVertx(),client.domain(),fetchAll,client.host(),client.port());
        } else {
            throw new MissingResourceException("missing @EtcdClient annotation",verticleInstance.getClass().getName(),"");
        }
    }

    @Override
    public DiscoveryClientEtcd getClient(Vertx vertx, JsonObject config) {
        Objects.nonNull(vertx);
        Objects.nonNull(config);
        String host = config.getString("etcd-host",null);
        String domain = config.getString("domain",null);
        String port = config.getString("etcd-port",null);
        Objects.nonNull(host);
        Objects.nonNull(domain);
        Objects.nonNull(port);
        final URI fetchAll = URI.create("http://" + host + ":" + port + ETCD_BASE_PATH + domain + "/?recursive=true");
        return new DiscoveryClientEtcd(vertx,domain,fetchAll,host,Integer.valueOf(port));

    }
}
