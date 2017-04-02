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
 * Define the terminal onFailure onSuccess which will be called when lookup fails.
 */
public class FailureDiscovery extends ExecuteDiscovery {


    public FailureDiscovery(Vertx vertx,
                            DiscoveryClient client,
                            String serviceName,
                            Consumer<NodeResponse> onSuccess,
                            Consumer<NodeResponse> onFailure,
                            Consumer<NodeResponse> onError, int amount, long delay) {
        super(vertx, client, serviceName, onSuccess, onFailure, onError, amount, delay);
    }



    /**
     * Terminal on failure method which is called after retries are failed
     *
     * @param onFailure the onSuccess executed when no entry was found
     * @return {@link RetryDiscovery}, define the amount of retries
     */
    public RetryDiscovery onFailure(Consumer<NodeResponse> onFailure) {
        return new RetryDiscovery(vertx, client, serviceName, onSuccess, onFailure, onError);
    }


}
