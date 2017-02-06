package org.jacpfx.vertx.rest.interfaces.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by amo on 31.01.17.
 */

public interface RetryExecutor<T> {
    void execute(String methodId,
                 String id,
                 Object message,
                 ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                 DeliveryOptions deliveryOptions,
                 Vertx vertx, Throwable t,
                 Consumer<Throwable> errorMethodHandler,
                 RoutingContext context,
                 Map<String, String> headers,
                 Encoder encoder,
                 Consumer<Throwable> errorHandler,
                 ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                 int httpStatusCode,
                 int httpErrorCode, int retryCount,
                 long timeout, long circuitBreakerTimeout);
}