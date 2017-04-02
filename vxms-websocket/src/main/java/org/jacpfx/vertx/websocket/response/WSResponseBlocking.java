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
import org.jacpfx.vertx.websocket.util.CommType;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.12.15.
 * The TargetType defines the target of the (blocking) response.
 */
public class WSResponseBlocking {
    protected final WebSocketEndpoint endpoint;
    protected final Vertx vertx;
    protected final WebSocketRegistry registry;
    protected final Consumer<Throwable> errorMethodHandler;

    protected WSResponseBlocking(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.errorMethodHandler = errorMethodHandler;
    }


    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking toAll() {
        return new ResponseTypeBlocking(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking toAllBut(WebSocketEndpoint... endpoints) {
        return new ResponseTypeBlocking(endpoints, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking reply() {
        return new ResponseTypeBlocking(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking to(WebSocketEndpoint... endpoints) {
        return new ResponseTypeBlocking(endpoints, vertx, CommType.TO, errorMethodHandler, registry);
    }


}
