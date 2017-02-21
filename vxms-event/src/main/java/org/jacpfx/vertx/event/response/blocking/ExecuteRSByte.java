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
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusByteCallBlocking;
import org.jacpfx.vertx.event.response.basic.ExecuteRSBasicByte;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByte extends ExecuteRSBasicByte {
    protected final long delay;
    protected final ExecuteEventbusByteCallBlocking excecuteAsyncEventBusAndReply;
    protected final ThrowableSupplier<byte[]> byteSupplier;
    protected final ThrowableFunction<Throwable, byte[]> onFailureRespond;

    public ExecuteRSByte(String methodId,
                         Vertx vertx,
                         Throwable t,
                         Consumer<Throwable> errorMethodHandler,
                         Message<Object> message,
                         ThrowableSupplier<byte[]> byteSupplier,
                         ExecuteEventbusByteCallBlocking excecuteAsyncEventBusAndReply,
                         Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond,
                         DeliveryOptions deliveryOptions,
                         int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId,
                vertx, t,
                errorMethodHandler, message,
                null,
                null,
                errorHandler, null,
                deliveryOptions, retryCount,
                timeout, circuitBreakerTimeout);
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
        this.byteSupplier = byteSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(DeliveryOptions deliveryOptions) {
        Objects.requireNonNull(deliveryOptions);
        final ExecuteRSByte lastStep = new ExecuteRSByte(methodId, vertx, t,
                errorMethodHandler,
                message,
                byteSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler, onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout, delay, circuitBreakerTimeout);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(methodId,vertx, errorMethodHandler, message, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler -> executeAsync(supplier, retry, handler), false, getAsyncResultHandler(retry));
                        }

                );


    }

    private void executeAsync(ThrowableSupplier<byte[]> supplier, int retry, Future<ExecutionResult<byte[]>> handler) {
        ResponseBlockingExecution.executeRetryAndCatchAsync(methodId, supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx, t, retry, timeout, circuitBreakerTimeout, delay);
    }

    private Handler<AsyncResult<ExecutionResult<byte[]>>> getAsyncResultHandler(int retry) {
        return value -> {
            if (!value.failed()) {
                respond(value.result().getResult());
            } else {
               if(retry==0)fail(value.cause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }

        };
    }


}
