package org.jacpfx.vertx.registry;

import io.vertx.core.Vertx;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 */
public class RetryDiscovery extends ExecuteDiscovery {


    public RetryDiscovery(Vertx vertx, DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, Consumer<NodeResponse> onError) {
        super(vertx,client, serviceName, consumer, onFailure, onError, 0, 0);
    }


    public DelayDiscovery retry(int amount) {
        return new DelayDiscovery(vertx, client, serviceName, consumer, onFailure,onError, amount);
    }
}
