package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;
import org.jacpfx.vertx.rest.util.EventbusAsyncByteExecutionUtil;
import org.jacpfx.vertx.rest.util.EventbusAsyncObjectExecutionUtil;
import org.jacpfx.vertx.rest.util.EventbusAsyncStringExecutionUtil;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusAsyncResponse {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String id;
    private final Object message;
    private final DeliveryOptions options;
    private final Function<AsyncResult<Message<Object>>, ?> errorFunction;


    public EventBusAsyncResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.id = id;
        this.message = message;
        this.options = options;
        this.errorFunction = errorFunction;
    }


    public ExecuteRSStringResponse mapToStringResponse(ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
        return EventbusAsyncStringExecutionUtil.mapToStringResponse(id, message,options,errorFunction,stringFunction,vertx, t, errorMethodHandler, context, null, null,  null, null, null, 0, 0, 0, 0);
    }

    public ExecuteRSByteResponse mapToByteResponse(ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventbusAsyncByteExecutionUtil.mapToByteResponse(id,message,options,errorFunction,byteFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0,0,0);
    }

    public ExecuteRSObjectResponse mapToObjectResponse(ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventbusAsyncObjectExecutionUtil.mapToObjectResponse(id,message,options,errorFunction,objectFunction, vertx, t, errorMethodHandler, context, null, null, encoder, null, null, 0, 0,0,0);
    }


    public EventBusAsyncResponse deliveryOptions(DeliveryOptions options) {
        return new EventBusAsyncResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }

    public EventBusAsyncResponse onErrorResult(Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        return new EventBusAsyncResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }


}
