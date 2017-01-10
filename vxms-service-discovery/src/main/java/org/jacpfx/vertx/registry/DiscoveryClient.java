package org.jacpfx.vertx.registry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import or.jacpfx.spi.DiscoveryClientSpi;

import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 23.06.16.
 * The discovery Client interface
 */
public interface DiscoveryClient {

    /**
     * find service by name by using a builder to define error handling und further execution parameters
     *
     * @param serviceName
     * @return DCServiceName
     */
    OnSuccessDiscovery find(String serviceName);

    /**
     * simple find service node method
     *
     * @param serviceName the service name to lookup
     * @param consumer    the consumer holding the service node
     */
    void findNode(String serviceName, Consumer<NodeResponse> consumer);


    /**
     * Creates a discovery client instance
     *
     * @param verticle the current verticle
     * @return the discover client implementation
     */
    static DiscoveryClient createClient(AbstractVerticle verticle) {
        ServiceLoader<DiscoveryClientSpi> loader = ServiceLoader.load(DiscoveryClientSpi.class);
        if (!loader.iterator().hasNext()) return null;
        return (DiscoveryClient) loader.iterator().next().getClient(verticle);
    }

    /**
     * Create a discovery client instance
     *
     * @param vertx         the vertx instance
     * @param clientOptions the client options
     * @param config        the client configuration
     * @return the discover client implementation
     */
    static DiscoveryClient createClient(Vertx vertx, HttpClientOptions clientOptions, JsonObject config) {
        ServiceLoader<DiscoveryClientSpi> loader = ServiceLoader.load(DiscoveryClientSpi.class);
        if (!loader.iterator().hasNext()) return null;
        return (DiscoveryClient) loader.iterator().next().getClient(vertx, clientOptions, config);
    }

    /**
     * check if client is connected to discovery server, accepts consumer and should not block the thread
     *
     * @param  connected, provide Lambda expression to check if connection was successful
     */
    void isConnected(Consumer<Future<?>> connected);
}
