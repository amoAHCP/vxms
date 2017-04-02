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

package org.jacpfx.vertx.event.eventbus.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.throwable.ThrowableFunction;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusByteResponse;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusObjectResponse;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusStringResponse;
import org.jacpfx.vertx.event.util.EventbusByteExecutionBlockingUtil;
import org.jacpfx.vertx.event.util.EventbusObjectExecutionBlockingUtil;
import org.jacpfx.vertx.event.util.EventbusStringExecutionBlockingUtil;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 * Represents the start of a blocking execution chain
 */
public class EventbusBridgeBlockingResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> requestmessage;
    private final String targetId;
    private final Object message;
    private final DeliveryOptions options;

    /**
     * Pass all parameters to execute the chain
     *
     * @param methodId           the method identifier
     * @param requestmessage     the message to responde
     * @param vertx              the vertx instance
     * @param failure            the last failure
     * @param errorMethodHandler the error-method handler
     * @param targetId           the event-bus message target-targetId
     * @param message            the event-bus message
     * @param options            the event-bus delivery options
     */
    public EventbusBridgeBlockingResponse(String methodId,
                                          Message<Object> requestmessage,
                                          Vertx vertx,
                                          Throwable failure,
                                          Consumer<Throwable> errorMethodHandler,
                                          String targetId,
                                          Object message,
                                          DeliveryOptions options) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.requestmessage = requestmessage;
        this.targetId = targetId;
        this.message = message;
        this.options = options;
    }

    /**
     * Maps the event-bus response to a String response for the REST request
     *
     * @param stringFunction the function, that takes the response message from the event bus and that maps it to a valid eventbus response
     * @return the execution chain {@link ExecuteEventbusStringResponse}
     */
    public ExecuteEventbusStringResponse mapToStringResponse(ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
        return EventbusStringExecutionBlockingUtil.mapToStringResponse(methodId,
                targetId,
                message,
                stringFunction,
                options,
                vertx, t,
                errorMethodHandler,
                null,
                null,
                null,
                null,
                null,
                0, 0l,
                0l, 0l);
    }

    /**
     * Maps the event-bus response to a byte response for the REST request
     *
     * @param byteFunction the function, that takes the response message from the event bus and that maps it to a valid eventbus response
     * @return the execution chain {@link ExecuteEventbusByteResponse}
     */
    public ExecuteEventbusByteResponse mapToByteResponse(ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventbusByteExecutionBlockingUtil.mapToByteResponse(methodId,
                targetId,
                message,
                byteFunction,
                options,
                vertx, t,
                errorMethodHandler,
                null,
                null,
                null,
                null,
                null,
                0, 0l,
                0l, 0l);
    }

    /**
     * Maps the event-bus response to a byte response for the REST request
     *
     * @param objectFunction the function, that takes the response message from the event bus and that maps it to a valid eventbus response
     * @param encoder        the encoder to serialize your object response
     * @return the execution chain {@link ExecuteEventbusObjectResponse}
     */
    public ExecuteEventbusObjectResponse mapToObjectResponse(ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventbusObjectExecutionBlockingUtil.mapToObjectResponse(methodId, targetId,
                message,
                objectFunction,
                options,
                vertx, t,
                errorMethodHandler,
                null,
                null, encoder,
                null,
                null,
                null,
                0, 0l,
                0l, 0l);
    }


}
