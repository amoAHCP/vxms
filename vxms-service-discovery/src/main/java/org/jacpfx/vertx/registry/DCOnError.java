package org.jacpfx.vertx.registry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 */
public class DCOnError extends DCExecute{



    public DCOnError(DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Runnable onError) {
        super(client,serviceName,consumer,onError,0,0);
    }


    public DCRetry retry(int amount){
        return new DCRetry(client,serviceName,consumer,onError,amount);
    }
}
