package org.jacpfx.vertx.registry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 */
public class DCServiceName {

    private final DiscoveryClient client;
    private final String serviceName;

    public DCServiceName(DiscoveryClient client, String serviceName) {
        this.client = client;
        this.serviceName = serviceName;
    }


    public DCExecute onSuccess(Consumer<NodeResponse> consumer){
         return new DCExecute(client,serviceName,consumer,null,0,0);
    }


}
