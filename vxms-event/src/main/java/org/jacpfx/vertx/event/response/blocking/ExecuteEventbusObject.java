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
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusObjectCallBlocking;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusObject extends ExecuteEventbusBasicObject {
    protected final long delay;
    protected final long timeout;
    protected final ExecuteEventbusObjectCallBlocking excecuteEventBusAndReply;
    protected final ThrowableSupplier<Serializable> objectSupplier;
    protected final ThrowableFunction<Throwable, Serializable> onFailureRespond;

    public ExecuteEventbusObject(String methodId,
                                 Vertx vertx, Throwable t,
                                 Consumer<Throwable> errorMethodHandler,
                                 Message<Object> message,
                                 ThrowableSupplier<Serializable> objectSupplier,
                                 ExecuteEventbusObjectCallBlocking excecuteEventBusAndReply,
                                 Encoder encoder,
                                 Consumer<Throwable> errorHandler,
                                 ThrowableFunction<Throwable, Serializable> onFailureRespond,
                                 DeliveryOptions deliveryOptions, int retryCount,
                                 long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t,
                errorMethodHandler,
                message,
                null,
                null,
                encoder,
                errorHandler,
                null, deliveryOptions,
                retryCount, timeout, circuitBreakerTimeout);
        this.delay = delay;
        this.timeout = timeout;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.objectSupplier = objectSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(DeliveryOptions deliveryOptions) {
        Objects.requireNonNull(deliveryOptions);
        final ExecuteEventbusObject lastStep = new ExecuteEventbusObject(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, delay, timeout, circuitBreakerTimeout);
        lastStep.execute();
    }



    @Override
    public void execute() {
        Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(methodId,vertx, errorMethodHandler, message, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        Optional.ofNullable(objectSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler -> executeAsync(supplier, retry, handler), false, getAsyncResultHandler(retry));
                        }

                );

    }

    private void executeAsync(ThrowableSupplier<Serializable> supplier, int retry, Future<ExecutionResult<Serializable>> handler) {
        ResponseBlockingExecution.executeRetryAndCatchAsync(methodId, supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx, t, retry, timeout, 0l, delay);
    }

    private Handler<AsyncResult<ExecutionResult<Serializable>>> getAsyncResultHandler(int retry) {
        return value -> {
            if (!value.failed()) {
                respond(value.result().getResult());
            } else {
                if(retry==0)fail(value.cause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
        };
    }


}
