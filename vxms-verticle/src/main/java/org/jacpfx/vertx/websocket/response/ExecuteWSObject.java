package org.jacpfx.vertx.websocket.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
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
 * This class defines several error methods for the createResponse methods and executes the async createResponse chain.
 */
public class ExecuteWSObject extends ExecuteWSBasicObject {
    protected final long delay;
    protected final long timeout;

    protected ExecuteWSObject(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;
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


}