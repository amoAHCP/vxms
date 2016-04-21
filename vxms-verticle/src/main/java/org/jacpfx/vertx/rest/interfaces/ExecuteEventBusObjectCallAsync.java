package org.jacpfx.vertx.rest.interfaces;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 21.03.16.
 */
@FunctionalInterface
public interface ExecuteEventBusObjectCallAsync {

    void execute(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                 Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                 Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount,long timeout, long delay);
}
