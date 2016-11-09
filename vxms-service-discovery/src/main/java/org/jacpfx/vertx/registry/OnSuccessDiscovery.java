package org.jacpfx.vertx.registry;

import io.vertx.core.Vertx;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 * Defines a builder API for defining lookup operation for service discovery.
 */
public class OnSuccessDiscovery {

    private final DiscoveryClient client;
    private final String serviceName;
    private final Vertx vertx;

    public OnSuccessDiscovery(Vertx vertx, DiscoveryClient client, String serviceName) {
        this.vertx = vertx;
        this.client = client;
        this.serviceName = serviceName;
    }


    /**
     * define consumer for handling the NodeResponse if Client finds a valid entry
     *
     * @param consumer
     * @return {@link ErrorDiscovery} the next step, define onError or onFailure
     */
    public ErrorDiscovery onSuccess(Consumer<NodeResponse> consumer) {
        return new ErrorDiscovery(vertx, client, serviceName, consumer, null, null, 0, 0);
    }


}
