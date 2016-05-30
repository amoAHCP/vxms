package org.jacpfx.vertx.registry;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 30.05.16.
 */
public class DCExecute {
    protected final DiscoveryClient client;
    protected final String serviceName;
    protected final Consumer<NodeResponse> consumer;
    protected final Runnable onError;
    protected final int amount;
    protected final long delay;


    public DCExecute(DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Runnable onError, int amount, long delay) {
        this.client = client;
        this.serviceName = serviceName;
        this.consumer = consumer;
        this.onError = onError;
        this.amount = amount;
        this.delay = delay;
    }


    public void execute() {
        Optional.ofNullable(client).ifPresent(client -> {
            Optional.ofNullable(serviceName).ifPresent(name -> {
                findAndHandle(client, name, consumer, onError,amount,delay);
            });
        });
    }

    protected void findAndHandle(DiscoveryClient client, String name, Consumer<NodeResponse> consumer,Runnable onError, int amount,long delay) {
        client.getVertx().runOnContext(handler -> {
            client.findNode(name, resopnse -> {
                if(resopnse.succeeded()){
                    consumer.accept(resopnse);
                } else {
                    Optional.ofNullable(onError).ifPresent(errorHandler -> {
                        if(delay>0 && amount > 0){
                            client.getVertx().executeBlocking(blocking -> {
                                sleep(delay);
                                findAndHandle(client,name,consumer,errorHandler,amount-1,delay);
                                blocking.complete();
                            }, result -> {

                            });
                        } else if(delay ==0L && amount > 0) {
                            findAndHandle(client,name,consumer,errorHandler,amount-1,delay);
                        } else {
                            errorHandler.run();
                        }
                    });

                }
            });
        });

    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public DCOnError onError(Runnable onError){
        return new DCOnError(client,serviceName,consumer,onError);
    }
}
