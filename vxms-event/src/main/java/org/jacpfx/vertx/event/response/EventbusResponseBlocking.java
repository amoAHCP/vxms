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

package org.jacpfx.vertx.event.response;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.throwable.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusByteResponse;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusObjectResponse;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusStringResponse;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API to define a Task and to reply the request with the output of your task.
 */
public class EventbusResponseBlocking {
    private final String methodId;
    private final VxmsShared vxmsShared;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> message;

    /**
     * The constructor to pass all needed members
     *
     * @param methodId           the method identifier
     * @param message            the event-bus message to respond to
     * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
     * objects per instance
     * @param failure            the failure thrown while task execution
     * @param errorMethodHandler the error handler
     */
    public EventbusResponseBlocking(String methodId, Message<Object> message, VxmsShared vxmsShared, Throwable failure, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.vxmsShared = vxmsShared;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.message = message;

    }


    /**
     * Retunrs a byte array to the target type
     *
     * @param byteSupplier supplier which returns the createResponse value as byte array
     * @return {@link ExecuteEventbusByteResponse}
     */
    public ExecuteEventbusByteResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteEventbusByteResponse(methodId, vxmsShared, failure, errorMethodHandler, message, byteSupplier, null, null, null, null, 0, 0l, 0l, 0l);
    }

    /**
     * Retunrs a String to the target type
     *
     * @param stringSupplier supplier which returns the createResponse value as String
     * @return {@link ExecuteEventbusStringResponse}
     */
    public ExecuteEventbusStringResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteEventbusStringResponse(methodId, vxmsShared, failure, errorMethodHandler, message, stringSupplier, null, null, null, null, 0, 0l, 0l, 0l);
    }

    /**
     * Retunrs a Serializable to the target type
     *
     * @param objectSupplier supplier which returns the createResponse value as Serializable
     * @param encoder        the encoder to serialize the response object
     * @return {@link ExecuteEventbusObjectResponse}
     */
    public ExecuteEventbusObjectResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteEventbusObjectResponse(methodId, vxmsShared, failure, errorMethodHandler, message, objectSupplier, null, encoder, null, null, null, 0, 0, 0l, 0l);
    }
}
