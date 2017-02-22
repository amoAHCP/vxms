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
public interface ExecuteEventbusByteCallBlocking {

    void execute(String _methodId,
                 Vertx _vertx,
                 Consumer<Throwable> _errorMethodHandler,
                 Message<Object> _requestMessage,
                 Consumer<Throwable> _errorHandler,
                 ThrowableFunction<Throwable, byte[]> onFailureRespond,
                 DeliveryOptions _responseDeliveryOptions,
                 int _retryCount, long _timeout, long _delay, long _circuitBreakerTimeout);
}
