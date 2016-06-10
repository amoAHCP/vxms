package org.jacpfx.vertx.registry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 */
public class DCOnFailure extends DCExecute{



    public DCOnFailure(DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onError) {
        super(client,serviceName,consumer,onError,0,0);
    }


    public DCRetry retry(int amount){
        return new DCRetry(client,serviceName,consumer,onFailure,amount);
    }
}
