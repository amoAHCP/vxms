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
    protected final Consumer<NodeResponse> onFailure;
    protected final int amount;
    protected final long delay;


    public DCExecute(DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        this.client = client;
        this.serviceName = serviceName;
        this.consumer = consumer;
        this.onFailure = onFailure;
        this.amount = amount;
        this.delay = delay;
    }


    public void execute() {
        Optional.ofNullable(client).
                ifPresent(client -> Optional.ofNullable(serviceName).
                        ifPresent(name -> findAndHandle(client, name, consumer, onFailure, amount, delay)));
    }

    protected void findAndHandle(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        client.getVertx().runOnContext(handler ->
                client.findNode(name, response -> {
                    if (response.succeeded()) {
                        consumer.accept(response);
                    } else {
                        Optional.ofNullable(onFailure).ifPresent(errorHandler -> {
                            if (delay > 0 && amount > 0) {
                                client.getVertx().executeBlocking(blocking -> {
                                    sleep(delay);
                                    blocking.complete();
                                }, result -> findAndHandle(client, name, consumer, errorHandler, amount - 1, delay));
                            } else if (delay == 0L && amount > 0) {
                                findAndHandle(client, name, consumer, errorHandler, amount - 1, delay);
                            } else {
                                errorHandler.accept(response);
                            }
                        });

                    }
                }));

    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public DCOnFailure onFailure(Consumer<NodeResponse> onFailure) {
        return new DCOnFailure(client, serviceName, consumer, onFailure);
    }
}
