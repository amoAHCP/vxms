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

package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.11.15.
 * The WebSocketHandler delivers access to the Socket Endpoint, the payload and the response.
 */
public class WebSocketHandler {
    private final WebSocketEndpoint endpoint;
    private final Vertx vertx;
    private final WebSocketRegistry registry;
    private final Consumer<Throwable> errorMethodHandler;
    private final byte[] value;


    public WebSocketHandler(WebSocketRegistry registry, WebSocketEndpoint endpoint, byte[] value, Vertx vertx, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.value = value;
        this.errorMethodHandler = errorMethodHandler;
    }

    /**
     * Returns the Endpoint definition with URL and handler id
     *
     * @return {@see WSEndpoint}
     */
    public WebSocketEndpoint endpoint() {
        return this.endpoint;
    }

    /**
     * Returns the payload handler which gives you access to the payload transmitted
     *
     * @return {@see Payload}
     */
    public Payload payload() {
        return new Payload(value);
    }


    /**
     * Returns the start of the response chain.
     *
     * @return the target type of your response
     */
    public WSResponse response() {
        return new WSResponse(endpoint, vertx, registry, errorMethodHandler);
    }

    // TODO implement chain: send(), consume()
    public WSEventBusRequest eventBusRequest() {
        return new WSEventBusRequest(endpoint, vertx, registry, errorMethodHandler);

    }


}
