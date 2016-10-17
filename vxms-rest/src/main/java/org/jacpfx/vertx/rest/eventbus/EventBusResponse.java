package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObjectResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;
import org.jacpfx.vertx.rest.util.EventbusByteExecutionUtil;
import org.jacpfx.vertx.rest.util.EventbusObjectExecutionUtil;
import org.jacpfx.vertx.rest.util.EventbusStringExecutionUtil;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusResponse {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String id;
    private final Object message;
    private final DeliveryOptions options;


    public EventBusResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, String id,
                            Object message, DeliveryOptions options) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.id = id;
        this.message = message;
        this.options = options;
    }

    /**
     * Map Response from event-bus call to REST response
     *
     * @param stringFunction pass io.vertx.core.AsyncResult and future to complete with a String
     * @return the response chain
     */
    public ExecuteRSBasicStringResponse mapToStringResponse(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction) {

        return EventbusStringExecutionUtil.mapToStringResponse(id, message, options, stringFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0, 0);
    }

    /**
     * Map Response from event-bus call to REST response
     *
     * @param byteFunction pass io.vertx.core.AsyncResult and future to complete with a byte[] array
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse mapToByteResponse(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventbusByteExecutionUtil.mapToByteResponse(id, message, options, byteFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0, 0);
    }

    /**
     * Map Response from event-bus call to REST response
     *
     * @param objectFunction pass io.vertx.core.AsyncResult and future to complete with a Object
     * @param encoder        the Object encoder
     * @return the response chain
     */
    public ExecuteRSBasicObjectResponse mapToObjectResponse(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventbusObjectExecutionUtil.mapToObjectResponse(id, message, options, objectFunction, vertx, t, errorMethodHandler, context, null, null, encoder, null, null, 0, 0, 0);
    }


}
