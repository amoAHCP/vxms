package org.jacpfx.vertx.websocket.registry;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import org.jacpfx.common.util.Serializer;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.11.15.
 */
public interface WSRegistry {
    public static final String WS_REGISTRY = "wsRegistry";
    public static final String WS_ENDPOINT_HOLDER = "wsEndpointHolder";
    public static final String WS_LOCK = "wsLock";
    public static final String REGISTRY = "registry";
    void removeAndExecuteOnClose(ServerWebSocket serverSocket, Runnable onFinishRemove);

    void findEndpointsAndExecute(WSEndpoint currentEndpoint, Consumer<WSEndpoint> onFinishRegistration);

    void registerAndExecute(ServerWebSocket serverSocket, Consumer<WSEndpoint> onFinishRegistration);

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
