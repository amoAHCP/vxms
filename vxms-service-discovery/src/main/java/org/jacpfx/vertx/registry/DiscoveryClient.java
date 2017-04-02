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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import or.jacpfx.spi.DiscoveryClientSpi;
import org.jacpfx.vertx.registry.discovery.OnSuccessDiscovery;
import org.jacpfx.vertx.registry.nodes.NodeResponse;

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
     * @param serviceName the service name
     * @return the discovery execution chain
     */
    OnSuccessDiscovery find(String serviceName);

    /**
     * simple find service node method
     *
     * @param serviceName the service name to lookup
     * @param consumer    the onSuccess holding the service node
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
     * check if client is connected to discovery server, accepts onSuccess and should not block the thread
     *
     * @param  connected, provide Lambda expression to check if connection was successful
     */
    void isConnected(Consumer<Future<?>> connected);
}
