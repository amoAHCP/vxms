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

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 * Execute service discovery lookup
 */
public class ExecuteDiscovery {
    private static final long DEFAULT_LONG = 0L;
    private static final int DEFAULT_INT = 0;
    protected final Vertx vertx;
    protected final DiscoveryClient client;
    protected final String serviceName;
    protected final Consumer<NodeResponse> onSuccess;
    protected final Consumer<NodeResponse> onFailure;
    protected final Consumer<NodeResponse> onError;
    protected final int amount;
    protected final long delay;


    public ExecuteDiscovery(Vertx vertx, DiscoveryClient client, String serviceName, Consumer<NodeResponse> onSuccess, Consumer<NodeResponse> onFailure, Consumer<NodeResponse> onError, int amount, long delay) {
        this.vertx = vertx;
        this.client = client;
        this.serviceName = serviceName;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.onError = onError;
        this.amount = amount;
        this.delay = delay;
    }

    /**
     * Execute the lookup
     */
    public void execute() {
        Optional.ofNullable(client).
                ifPresent(client -> Optional.ofNullable(serviceName).
                        ifPresent(name -> findAndHandle(client, name, onSuccess, onFailure, amount, delay)));
    }


    protected void findAndHandle(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        vertx.runOnContext(handler ->
                client.findNode(name, response -> {
                    if (response.succeeded()) {
                        consumer.accept(response);
                    } else {
                        handleError(client, name, consumer, onFailure, amount, delay, response);
                    }
                }));

    }

    private void handleError(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay, NodeResponse response) {
        if (onError != null) onError.accept(response);
        if (delay > DEFAULT_LONG && amount > DEFAULT_INT) {
            retryAndDelay(client, name, consumer, onFailure, amount, delay);
        } else if (delay == DEFAULT_LONG && amount > DEFAULT_INT) {
            retry(client, name, consumer, onFailure, amount, delay);
        } else {
            if (onFailure != null) onFailure.accept(response);
        }
    }

    private void retry(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        final int decrementedAmount = amount - 1;
        findAndHandle(client, name, consumer, onFailure, decrementedAmount, delay);
    }

    private void retryAndDelay(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        vertx.executeBlocking(blocking -> {
            sleep(delay);
            blocking.complete();
        }, result -> {
            final int decrementedAmount = amount - 1;
            findAndHandle(client, name, consumer, onFailure, decrementedAmount, delay);
        });
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
