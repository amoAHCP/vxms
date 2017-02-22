package org.jacpfx.vertx.event.interfaces.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.03.16.
 */
@FunctionalInterface
public interface ExecuteEventbusObjectCall {

    void execute(String methodId,
                 Vertx vertx,
                 Consumer<Throwable> errorMethodHandler,
                 Message<Object> requestMessage,
                 Encoder encoder,
                 Consumer<Throwable> errorHandler,
                 ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
                 DeliveryOptions responseDeliveryOptions,
                 int retryCount, long timeout, long circuitBreakerTimeout);
}
