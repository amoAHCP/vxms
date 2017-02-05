package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.*;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by amo on 31.01.17.
 */

public interface RecursiveBlockingExecutor<T> {
    void execute(String methodId,
                 Vertx vertx,
                 Throwable t,
                 Consumer<Throwable> errorMethodHandler,
                 RoutingContext context,
                 Map<String, String> headers,
                 ThrowableSupplier<T> supplier,
                 Encoder encoder, Consumer<Throwable> errorHandler,
                 ThrowableFunction<Throwable, T> onFailureRespond,
                 int httpStatusCode, int httpErrorCode,
                 int retryCount, long timeout,
                 long delay,
                 long circuitBreakerTimeout);
}