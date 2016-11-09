package org.jacpfx.vertx.registry;

import io.vertx.core.Vertx;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 * Defines the delay time between retries
 */
public class DelayDiscovery extends ExecuteDiscovery {



    public DelayDiscovery(Vertx vertx, DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, Consumer<NodeResponse> onError, int amount) {
        super(vertx,client,serviceName,consumer,onFailure,onError,amount,0);
    }


    /**
     * The delay time in ms before a retry
     * @param ms time in ms
     * @return {@link ExecuteDiscovery} execute chain
     */
    public ExecuteDiscovery delay(long ms){
        return new ExecuteDiscovery(vertx,client,serviceName,consumer,onFailure,onError,amount,ms);
    }


}
