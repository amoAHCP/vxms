package org.jacpfx.vertx.event.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ExecutionResult;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.event.response.basic.ExecuteRSBasicString;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusStringCallBlocking;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSString extends ExecuteRSBasicString {
    protected final long delay;
    protected final ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final ThrowableFunction<Throwable, String> onFailureRespond;

    public ExecuteRSString(String methodId,
                           Vertx vertx,
                           Throwable t,
                           Consumer<Throwable> errorMethodHandler,
                           Message<Object> message,
                           ThrowableSupplier<String> stringSupplier,
                           ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply,
                           Consumer<Throwable> errorHandler,
                           ThrowableFunction<Throwable, String> onFailureRespond,
                           DeliveryOptions deliveryOptions,
                           int retryCount,long timeout,
                           long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, message, null, null, errorHandler, null,deliveryOptions, retryCount, timeout, circuitBreakerTimeout); // TODO define circuitBreakerTimout!!
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
        this.stringSupplier = stringSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(DeliveryOptions deliveryOptions) {
        Objects.requireNonNull(deliveryOptions);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId, vertx, t, errorMethodHandler,message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay,circuitBreakerTimeout);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(methodId, vertx, errorMethodHandler, message, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay,circuitBreakerTimeout);
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
        ResponseBlockingExecution.executeRetryAndCatchAsync(methodId,supplier, blockingHandler, errorHandler, onFailureRespond, errorMethodHandler, vertx, t, retry, timeout, circuitBreakerTimeout, delay);
    }

    private Handler<AsyncResult<ExecutionResult<String>>> getAsyncResultHandler(int retry) {
        return value -> {
            if (!value.failed()) {
                ExecutionResult<String> result = value.result();
                if(!result.handledError()){
                    respond(result.getResult());
                } else {
                    if(retry==0)fail(result.getResult(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                }

            } else {
                if(retry==0)fail(value.cause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
        };
    }

}
