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

package org.jacpfx.vertx.websocket.response.basic;

import io.vertx.core.Vertx;
import org.jacpfx.common.throwable.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the createResponse chain.
 */
public class ExecuteWSBasicObject {
    protected final WebSocketEndpoint[] endpoint;
    protected final Vertx vertx;
    protected final CommType commType;
    protected final ThrowableSupplier<Serializable> objectSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final Function<Throwable, Serializable> errorHandlerObject;
    protected final WebSocketRegistry registry;
    protected final int retryCount;


    protected ExecuteWSBasicObject(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.commType = commType;
        this.objectSupplier = objectSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorMethodHandler = errorMethodHandler;
        this.errorHandlerObject = errorHandlerObject;
        this.registry = registry;
        this.retryCount = retryCount;
    }


    /**
     * Executes the response chain
     */
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;
        Optional.ofNullable(objectSupplier).
                ifPresent(supplier ->
                        Optional.ofNullable(WebSocketExecutionUtil.executeRetryAndCatch(supplier,
                                null,
                                errorHandler,
                                errorHandlerObject,
                                errorMethodHandler,
                                retry)
                        ).
                                ifPresent(value -> WebSocketExecutionUtil.encode(value, encoder).
                                        ifPresent(val -> WebSocketExecutionUtil.sendObjectResult(val, commType, vertx, registry, endpoint))));
    }


}