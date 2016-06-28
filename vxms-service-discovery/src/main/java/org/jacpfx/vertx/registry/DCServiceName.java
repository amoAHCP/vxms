package org.jacpfx.vertx.registry;

import io.vertx.core.Vertx;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 */
public class DCServiceName {

    private final DiscoveryClient client;
    private final String serviceName;
    private final Vertx vertx;

    public DCServiceName(Vertx vertx,DiscoveryClient client, String serviceName) {
        this.vertx = vertx;
        this.client = client;
        this.serviceName = serviceName;
    }


    public DCExecute onSuccess(Consumer<NodeResponse> consumer){
         return new DCExecute(vertx,client,serviceName,consumer,null,null,0,0);
    }


}
