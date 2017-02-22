package org.jacpfx.vertx.event.interfaces.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.03.16.
 */
@FunctionalInterface
public interface ExecuteEventbusStringCallBlocking {

    void execute(String methodId,
                 Vertx vertx,
                 Consumer<Throwable> errorMethodHandler,
                 Message<Object> requestMessage,
                 Consumer<Throwable> errorHandler,
                 ThrowableFunction<Throwable, String> onFailureRespond,
                 DeliveryOptions _responseDeliveryOptions,
                 int retryCount, long timeout, long delay, long circuitBreakerTimeout);
}
