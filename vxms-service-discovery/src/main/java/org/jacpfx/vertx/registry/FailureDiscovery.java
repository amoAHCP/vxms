package org.jacpfx.vertx.registry;

import io.vertx.core.Vertx;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16. Define the terminal onFailure consumer which will be called when lookup fails.
 */
public class FailureDiscovery extends ExecuteDiscovery {


    public FailureDiscovery(Vertx vertx, DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, Consumer<NodeResponse> onError, int amount, long delay) {
        super(vertx, client, serviceName, consumer, onFailure, onError, amount, delay);
    }



    /**
     * Terminal on failure method which is called after retries are failed
     *
     * @param onFailure
     * @return @see{RetryDiscovery}, define the amount of retries
     */
    public RetryDiscovery onFailure(Consumer<NodeResponse> onFailure) {
        return new RetryDiscovery(vertx, client, serviceName, consumer, onFailure, onError);
    }


}
