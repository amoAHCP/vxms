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
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventbusStringCallBlocking;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicString;
import org.jacpfx.vertx.rest.response.basic.ResponseExecution;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class is the end of the blocking fluent API, all data collected to execute the chain.
 */
public class ExecuteRSString extends ExecuteRSBasicString {
    protected final long delay;
    protected final ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final ThrowableFunction<Throwable, String> onFailureRespond;

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                      the method identifier
     * @param vertx                         the vertx instance
     * @param failure                       the failure thrown while task execution
     * @param errorMethodHandler            the error handler
     * @param context                       the vertx routing context
     * @param headers                       the headers to pass to the response
     * @param stringSupplier                the supplier, producing the byte response
     * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                       the encoder to encode your objects
     * @param errorHandler                  the error handler
     * @param onFailureRespond              the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode                the http status code to set for response
     * @param httpErrorCode                 the http error code to set in case of failure handling
     * @param retryCount                    the amount of retries before failure execution is triggered
     * @param timeout                       the amount of time before the execution will be aborted
     * @param delay                         the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout         the amount of time before the circuit breaker closed again
     */
    public ExecuteRSString(String methodId,
                           Vertx vertx,
                           Throwable failure,
                           Consumer<Throwable> errorMethodHandler,
                           RoutingContext context,
                           Map<String, String> headers,
                           ThrowableSupplier<String> stringSupplier,
                           ExecuteEventbusStringCallBlocking excecuteBlockingEventBusAndReply,
                           Encoder encoder,
                           Consumer<Throwable> errorHandler,
                           ThrowableFunction<Throwable, String> onFailureRespond,
                           int httpStatusCode,
                           int httpErrorCode,
                           int retryCount,
                           long timeout,
                           long delay,
                           long circuitBreakerTimeout) {
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                null,
                null,
                encoder,
                errorHandler,
                null,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteBlockingEventBusAndReply;
        this.stringSupplier = stringSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        new ExecuteRSString(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                status.code(),
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout).
                execute();
    }

    @Override
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        new ExecuteRSString(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                ResponseExecution.updateContentType(headers, contentType),
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                status.code(),
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout).
                execute();
    }


    @Override
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        new ExecuteRSString(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                ResponseExecution.updateContentType(headers, contentType),
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout).
                execute();
    }

    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx,
                        failure,
                        errorMethodHandler,
                        context,
                        headers,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount,
                        timeout,
                        delay,
                        circuitBreakerTimeout);
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
        ResponseBlockingExecution.executeRetryAndCatchAsync(methodId, supplier, blockingHandler, errorHandler, onFailureRespond, errorMethodHandler, vertx, failure, retry, timeout, circuitBreakerTimeout, delay);
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
