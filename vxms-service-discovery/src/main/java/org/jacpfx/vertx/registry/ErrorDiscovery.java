package org.jacpfx.vertx.registry;

import io.vertx.core.Vertx;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 * Defines the intermidiate onError consumer if lookup failes
 */
public class ErrorDiscovery extends FailureDiscovery {


    public ErrorDiscovery(Vertx vertx, DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, Consumer<NodeResponse> onError, int amount, long delay) {
        super(vertx, client, serviceName, consumer, onFailure, onError, amount, delay);
    }


    /**
     * Intermediate on failure method which is called on each error
     *
     * @param onError
     * @return {@link FailureDiscovery} the next step, define onFailure
     */
    public FailureDiscovery onError(Consumer<NodeResponse> onError) {
        return new FailureDiscovery(vertx, client, serviceName, consumer, onFailure, onError, 0, 0);
    }



}
