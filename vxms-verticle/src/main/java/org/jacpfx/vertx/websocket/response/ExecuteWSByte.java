package org.jacpfx.vertx.websocket.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the response methods and executes the async response chain.
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