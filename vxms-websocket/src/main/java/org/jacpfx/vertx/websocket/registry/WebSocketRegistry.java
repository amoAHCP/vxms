/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
