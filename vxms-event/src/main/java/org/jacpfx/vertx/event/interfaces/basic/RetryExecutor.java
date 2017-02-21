package org.jacpfx.vertx.event.interfaces.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by amo on 31.01.17.
 */

public interface RetryExecutor<T> {
    void execute(String _targetId,
                 Object _message,
                 ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> _function,
                 DeliveryOptions _requestDeliveryOptions,
                 String _methodId,
                 Vertx _vertx,
                 Throwable _t,
                 Consumer<Throwable> _errorMethodHandler,
                 Message<Object> _requestMessage,
                 ThrowableFutureConsumer<T> _consumer,
                 Encoder _encoder,
                 Consumer<Throwable> _errorHandler,
                 ThrowableErrorConsumer<Throwable, T> _onFailureRespond,
                 DeliveryOptions _responseDeliveryOptions,
                 int _retryCount, long _timeout, long _circuitBreakerTimeout);
}