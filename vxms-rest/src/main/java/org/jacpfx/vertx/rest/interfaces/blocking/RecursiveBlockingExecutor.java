package org.jacpfx.vertx.rest.interfaces.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;

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