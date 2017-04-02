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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the createResponse chain.
 */
public class ExecuteWSBasicStringResponse extends ExecuteWSBasicString{



    public ExecuteWSBasicStringResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, String> errorHandlerString, WebSocketRegistry registry, int retryCount) {
        super(endpoint,vertx,commType,stringSupplier,encoder,errorHandler,errorMethodHandler,errorHandlerString,registry,retryCount);
    }

    /**
     * defines an action if an error occurs, this is an intermediate method which will not result in an output value
     *
     * @param errorHandler the handler to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSBasicStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerString the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicString onFailureRespond(Function<Throwable, String> errorHandlerString) {
        return new ExecuteWSBasicString(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount);
    }


    /**
     * Defines how many times a retry will be done for the response method
     *
     * @param retryCount
     * @return the response chain
     */
    public ExecuteWSBasicStringResponse retry(int retryCount) {
        return new ExecuteWSBasicStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount);
    }


}