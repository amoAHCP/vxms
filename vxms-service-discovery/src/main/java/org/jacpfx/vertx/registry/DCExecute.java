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
    protected final Consumer<NodeResponse> onError;
    protected final int amount;
    protected final long delay;


    public DCExecute(DiscoveryClient client, String serviceName, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure,Consumer<NodeResponse> onError, int amount, long delay) {
        this.client = client;
        this.serviceName = serviceName;
        this.consumer = consumer;
        this.onFailure = onFailure;
        this.onError = onError;
        this.amount = amount;
        this.delay = delay;
    }


    public void execute() {
        Optional.ofNullable(client).
                ifPresent(client -> Optional.ofNullable(serviceName).
                        ifPresent(name -> findAndHandle(client, name, consumer, onFailure, amount, delay)));
    }

    /**
     * Intermediate on failure method which is called on each error
     * @param onError
     * @return
     */
    public DCOnFailure onError(Consumer<NodeResponse> onError) {
        return new DCOnFailure(client, serviceName, consumer, onFailure,onError);
    }

    /**
     * Terminal on failure method which is called after retries are failed
     * @param onFailure
     * @return
     */
    public DCOnFailure onFailure(Consumer<NodeResponse> onFailure) {
        return new DCOnFailure(client, serviceName, consumer, onFailure,onError);
    }

    protected void findAndHandle(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        client.getVertx().runOnContext(handler ->
                client.findNode(name, response -> {
                    if (response.succeeded()) {
                        consumer.accept(response);
                    } else {
                        handleError(client, name, consumer, onFailure, amount, delay, response);
                    }
                }));

    }

    private void handleError(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay, NodeResponse response) {
        if(onError!=null)onError.accept(response);
        if (delay > 0 && amount > 0) {
            retryAndDelay(client, name, consumer, onFailure, amount, delay);
        } else if (delay == 0L && amount > 0) {
            retry(client, name, consumer, onFailure, amount, delay);
        } else {
            if(onFailure!=null)onFailure.accept(response);
        }
    }

    private void retry(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        findAndHandle(client, name, consumer, onFailure, amount - 1, delay);
    }

    private void retryAndDelay(DiscoveryClient client, String name, Consumer<NodeResponse> consumer, Consumer<NodeResponse> onFailure, int amount, long delay) {
        client.getVertx().executeBlocking(blocking -> {
            sleep(delay);
            blocking.complete();
        }, result -> findAndHandle(client, name, consumer, onFailure, amount - 1, delay));
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
