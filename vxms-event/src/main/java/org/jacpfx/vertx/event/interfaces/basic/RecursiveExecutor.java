package org.jacpfx.vertx.event.interfaces.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;

import java.util.function.Consumer;

/**
 * Created by amo on 31.01.17.
 */

public interface RecursiveExecutor<T> {
    void execute(String methodId,
                 Vertx vertx,
                 Throwable t,
                 Consumer<Throwable> errorMethodHandler,
                 Message<Object> requestMessage,
                 ThrowableFutureConsumer<T> consumer,
                 Encoder encoder,
                 Consumer<Throwable> errorHandler,
                 ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                 DeliveryOptions responseDeliveryOptions,
                 int retryCount, long timeout, long circuitBreakerTimeout);
}