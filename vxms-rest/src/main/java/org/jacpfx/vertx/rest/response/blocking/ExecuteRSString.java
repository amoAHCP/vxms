package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ExecutionResult;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventBusStringCallBlocking;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicString;
import org.jacpfx.vertx.rest.response.basic.ResponseExecution;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSString extends ExecuteRSBasicString {
    protected final long delay;
    protected final ExecuteEventBusStringCallBlocking excecuteAsyncEventBusAndReply;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final ThrowableFunction<Throwable, String> onFailureRespond;

    public ExecuteRSString(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCallBlocking excecuteAsyncEventBusAndReply, Encoder encoder,
                           Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, null, null, encoder, errorHandler, null, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
        this.stringSupplier = stringSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId, vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        lastStep.execute();
    }

    @Override
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId, vertx, t, errorMethodHandler, context, ResponseExecution.updateContentType(headers, contentType), stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        lastStep.execute();
    }


    @Override
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId, vertx, t, errorMethodHandler, context, ResponseExecution.updateContentType(headers, contentType), stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        lastStep.execute();
    }

    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        Optional.ofNullable(stringSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler -> executeAsync(supplier, retry, handler), false, getAsyncResultHandler(retry));
                        }

                );
    }

    private void executeAsync(ThrowableSupplier<String> supplier, int retry, Future<ExecutionResult<String>> blockingHandler) {
        ResponseBlockingExecution.executeRetryAndCatchAsync(methodId, supplier, blockingHandler, errorHandler, onFailureRespond, errorMethodHandler, vertx, t, retry, timeout, circuitBreakerTimeout, delay);
    }

    private Handler<AsyncResult<ExecutionResult<String>>> getAsyncResultHandler(int retry) {
        return value -> {
            if (!value.failed()) {
                ExecutionResult<String> result = value.result();
                if (!result.handledError()) {
                    respond(result.getResult());
                } else {
                    respond(result.getResult(), httpErrorCode);
                }

            } else {
                checkAndCloseResponse(retry);
            }
        };
    }

}
