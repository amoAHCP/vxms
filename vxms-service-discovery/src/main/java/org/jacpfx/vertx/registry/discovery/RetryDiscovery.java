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
 * Defines the amount of retries before onFailure method is called or the lookup fails
 */
public class RetryDiscovery extends ExecuteDiscovery {


    public RetryDiscovery(Vertx vertx, DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, Consumer<NodeResponse> onError) {
        super(vertx,client, serviceName, consumer, onFailure, onError, 0, 0);
    }


    /**
     * Define the amount of retries
     * @param amount the amount of retries
     * @return {@link DelayDiscovery} define the delay time between retries
     */
    public DelayDiscovery retry(int amount) {
        return new DelayDiscovery(vertx, client, serviceName, onSuccess, onFailure,onError, amount);
    }
}
