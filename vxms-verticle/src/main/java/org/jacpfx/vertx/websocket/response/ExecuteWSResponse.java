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

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the response methods and executes the async response chain.
 */
public class ExecuteWSResponse extends ExecuteWSBasicResponse {
    private final long delay;
    protected final long timeout;

    protected ExecuteWSResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<byte[]> byteSupplier, ThrowableSupplier<String> stringSupplier, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, byte[]> errorHandlerByte, Function<Throwable, String> errorHandlerString, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse onByteResponseError(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse onStringResponseError(Function<Throwable, String> errorHandlerString) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse retry(int retryCount) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    public ExecuteWSResponse timeout(long timeout) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the response retries.
     * @param delay
     * @return the response chain
     */
    public ExecuteWSResponse delay(long delay) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount, timeout, delay);
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


        Optional.ofNullable(stringSupplier).
                ifPresent(supplier ->
                        handleStringResponse(retry, supplier));


        Optional.ofNullable(objectSupplier).
                ifPresent(supplier ->
                        handleObjectResponse(retry, supplier));

    }

    private void handleObjectResponse(int retry, ThrowableSupplier<Serializable> supplier) {
        this.vertx.executeBlocking(handler ->
                        WebSocketExecutionUtil.executeRetryAndCatchAsync(supplier,
                                handler,
                                "",
                                errorHandler,
                                errorHandlerObject,
                                vertx,
                                retry,
                                timeout, delay),
                false, (Handler<AsyncResult<Serializable>>) result ->
                        WebSocketExecutionUtil.handleExecutionResult(result,
                                errorMethodHandler, () -> Optional.ofNullable(result.result()).
                                        ifPresent(value -> WebSocketExecutionUtil.encode(value, encoder).
                                                ifPresent(val -> WebSocketExecutionUtil.sendObjectResult(val, commType, vertx, registry, endpoint))))
        );
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