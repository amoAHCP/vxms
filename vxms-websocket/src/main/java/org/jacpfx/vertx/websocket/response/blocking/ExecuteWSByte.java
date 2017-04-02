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

package org.jacpfx.vertx.websocket.response.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jacpfx.common.throwable.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.response.basic.ExecuteWSBasicByte;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the blocking createResponse chain.
 */
public class ExecuteWSByte extends ExecuteWSBasicByte {
    protected final long delay;
    protected final long timeout;

    protected ExecuteWSByte(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, byte[]> errorHandlerByte, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier ->
                        handleByteResponse(retry, supplier));


    }


    private void handleByteResponse(int retry, ThrowableSupplier<byte[]> supplier) {
        this.vertx.executeBlocking(handler ->
                        WebSocketExecutionUtil.executeRetryAndCatchAsync(supplier,
                                handler,
                                new byte[0],
                                errorHandler,
                                errorHandlerByte,
                                vertx,
                                retry,
                                timeout, delay),
                false,
                (Handler<AsyncResult<byte[]>>) result ->
                        WebSocketExecutionUtil.handleExecutionResult(result,
                                errorMethodHandler,
                                () -> Optional.ofNullable(result.result()).
                                        ifPresent(value -> WebSocketExecutionUtil.sendBinary(commType, vertx, registry, endpoint, value)))
        );
    }


}