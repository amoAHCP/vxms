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
import org.jacpfx.vertx.websocket.response.basic.ExecuteWSBasicString;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the blocking createResponse chain.
 */
public class ExecuteWSString extends ExecuteWSBasicString {
    protected final long delay;
    protected final long timeout;

    protected ExecuteWSString(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, String> errorHandlerString, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;

        Optional.ofNullable(stringSupplier).
                ifPresent(supplier ->
                        handleStringResponse(retry, supplier));


    }

    private void handleStringResponse(int retry, ThrowableSupplier<String> supplier) {
        this.vertx.executeBlocking(handler ->
                        WebSocketExecutionUtil.executeRetryAndCatchAsync(supplier,
                                handler,
                                "",
                                errorHandler,
                                errorHandlerString,
                                vertx,
                                retry,
                                timeout, delay),
                false, (Handler<AsyncResult<String>>) result ->
                        WebSocketExecutionUtil.handleExecutionResult(result,
                                errorMethodHandler,
                                () -> Optional.ofNullable(result.result()).
                                        ifPresent(value -> WebSocketExecutionUtil.sendText(commType, vertx, registry, endpoint, value)))
        );
    }


}