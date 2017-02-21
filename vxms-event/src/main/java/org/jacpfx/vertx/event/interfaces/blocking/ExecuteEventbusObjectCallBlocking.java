package org.jacpfx.vertx.event.interfaces.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.03.16.
 */
@FunctionalInterface
public interface ExecuteEventbusObjectCallBlocking {

    void execute(String _methodId,
                 Vertx _vertx,
                 Consumer<Throwable> _errorMethodHandler,
                 Message<Object> _requestMessage,
                 Encoder encoder,
                 Consumer<Throwable> _errorHandler,
                 ThrowableFunction<Throwable, Serializable> onFailureRespond,
                 DeliveryOptions _responseDeliveryOptions,
                 int retryCount, long timeout,
                 long delay, long circuitBreakerTimeout);
}
