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

package org.jacpfx.vertx.registry.discovery;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.registry.DiscoveryClient;
import org.jacpfx.vertx.registry.nodes.NodeResponse;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 * Defines a builder API for defining lookup operation for service discovery.
 */
public class OnSuccessDiscovery {

    private final DiscoveryClient client;
    private final String serviceName;
    private final Vertx vertx;

    /**
     * init discovery chain
     * @param vertx the vertx instance
     * @param client the discovery client
     * @param serviceName the name to discover
     */
    public OnSuccessDiscovery(Vertx vertx, DiscoveryClient client, String serviceName) {
        this.vertx = vertx;
        this.client = client;
        this.serviceName = serviceName;
    }


    /**
     * define onSuccess for handling the NodeResponse if Client finds a valid entry
     *
     * @param onSuccess the onSuccess executed on response
     * @return {@link ErrorDiscovery} the next step, define onError or onFailure
     */
    public ErrorDiscovery onSuccess(Consumer<NodeResponse> onSuccess) {
        return new ErrorDiscovery(vertx, client, serviceName, onSuccess, null, null, 0, 0);
    }


}
