package org.jacpfx.vertx.rest.eventbus.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;
import org.jacpfx.vertx.rest.util.EventBusByteExecutionBlockingUtil;
import org.jacpfx.vertx.rest.util.EventBusObjectExecutionBlockingUtil;
import org.jacpfx.vertx.rest.util.EventBusStringExecutionBlockingUtil;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusBlockingResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String id;
    private final Object message;
    private final DeliveryOptions options;


    public EventBusBlockingResponse(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, String id, Object message, DeliveryOptions options) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.id = id;
        this.message = message;
        this.options = options;
    }


    public ExecuteRSStringResponse mapToStringResponse(ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
        return EventBusStringExecutionBlockingUtil.mapToStringResponse(methodId,id, message, options, stringFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0,0, 0, 0l, 0l,0l);
    }

    public ExecuteRSByteResponse mapToByteResponse(ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventBusByteExecutionBlockingUtil.mapToByteResponse(methodId,id, message, options, byteFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0,0, 0l, 0l, 0l);
    }

    public ExecuteRSObjectResponse mapToObjectResponse(ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventBusObjectExecutionBlockingUtil.mapToObjectResponse(methodId,id, message, options, objectFunction, vertx, t, errorMethodHandler, context, null, null, encoder, null, null, 0,0, 0, 0l, 0l, 0l);
    }


}
