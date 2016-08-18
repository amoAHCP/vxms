package org.jacpfx.vertx.registry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import or.jacpfx.spi.DiscoveryClientSpi;

import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 23.06.16.
 */
public interface DiscoveryClient {

    /**
     * find service by name
     *
     * @param serviceName
     * @return DCServiceName
     */
    OnSuccessDiscovery find(String serviceName);


    void findNode(String serviceName, Consumer<NodeResponse> consumer);


    static DiscoveryClient createClient(AbstractVerticle verticle) {
        ServiceLoader<DiscoveryClientSpi> loader = ServiceLoader.load(DiscoveryClientSpi.class);
        if(!loader.iterator().hasNext()) return null;
        return (DiscoveryClient) loader.iterator().next().getClient(verticle);
    }

    static DiscoveryClient createClient(Vertx vertx, HttpClientOptions clientOptions, JsonObject config) {
        ServiceLoader<DiscoveryClientSpi> loader = ServiceLoader.load(DiscoveryClientSpi.class);
        if(!loader.iterator().hasNext()) return null;
        return (DiscoveryClient) loader.iterator().next().getClient(vertx,clientOptions,config);
    }

    boolean isConnected();
}
