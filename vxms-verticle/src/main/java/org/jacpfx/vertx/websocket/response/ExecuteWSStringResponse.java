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
public class ExecuteWSStringResponse extends ExecuteWSBasicStringResponse {
    private final long delay;
    protected final long timeout;

    protected ExecuteWSStringResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, String> errorHandlerString, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSStringResponse onStringResponseError(Function<Throwable, String> errorHandlerString) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSStringResponse retry(int retryCount) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    public ExecuteWSStringResponse timeout(long timeout) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the response retries.
     *
     * @param delay
     * @return the response chain
     */
    public ExecuteWSStringResponse delay(long delay) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
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