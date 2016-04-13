package org.jacpfx.vertx.websocket.registry;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import org.jacpfx.common.util.Serializer;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 17.11.15.
 */
public interface WebSocketRegistry {
    String WS_REGISTRY = "wsRegistry";
    String WS_ENDPOINT_HOLDER = "wsEndpointHolder";
    String WS_LOCK = "wsLock";
    String REGISTRY = "registry";
    void removeAndExecuteOnClose(ServerWebSocket serverSocket, Runnable onFinishRemove);

    void findEndpointsByURLAndExecute(WebSocketEndpoint currentEndpoint, Consumer<WebSocketEndpoint> executeOnMatch);

    void findEndpointsAndExecute(WebSocketEndpoint currentEndpoint, Function<WebSocketEndpoint, Boolean> filter, Consumer<WebSocketEndpoint> executeOnMatch);


    void registerAndExecute(ServerWebSocket serverSocket, Consumer<WebSocketEndpoint> onFinishRegistration);

    default byte[] serialize(Object payload) {
        try {
            return Serializer.serialize(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    default Object deserialize(byte[] payload) {
        try {
            return Serializer.deserialize(payload);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    default <T> Handler<AsyncResult<T>> onSuccess(Consumer<T> consumer) {
        return result -> {
            if (result.failed()) {
                result.cause().printStackTrace();

            } else {
                consumer.accept(result.result());
            }
        };
    }
}
