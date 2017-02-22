package org.jacpfx.vertx.event.eventbus.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicByteResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicObjectResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicStringResponse;
import org.jacpfx.vertx.event.util.EventbusByteExecutionUtil;
import org.jacpfx.vertx.event.util.EventbusObjectExecutionUtil;
import org.jacpfx.vertx.event.util.EventbusStringExecutionUtil;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventbusBridgeResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> requestmessage;
    private final String targetId;
    private final Object message;
    private final DeliveryOptions requestOptions;


    public EventbusBridgeResponse(String methodId, Message<Object> requestmessage, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, String targetId,
                                  Object message, DeliveryOptions requestOptions) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.requestmessage = requestmessage;
        this.targetId = targetId;
        this.message = message;
        this.requestOptions = requestOptions;
    }

    /**
     * Map Response from event-bus call to REST response
     *
     * @param stringFunction pass io.vertx.core.AsyncResult and future to complete with a String
     * @return the response chain
     */
    public ExecuteEventbusBasicStringResponse mapToStringResponse(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction) {

        return EventbusStringExecutionUtil.mapToStringResponse(methodId, targetId, message, stringFunction, requestOptions, vertx, t, errorMethodHandler, requestmessage,
                null, null, null, null, 0, 0l, 0l);
    }

    /**
     * Map Response from event-bus call to REST response
     *
     * @param byteFunction pass io.vertx.core.AsyncResult and future to complete with a byte[] array
     * @return the response chain
     */
    public ExecuteEventbusBasicByteResponse mapToByteResponse(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventbusByteExecutionUtil.mapToByteResponse(methodId, targetId, message, byteFunction, requestOptions, vertx, t, errorMethodHandler,
                requestmessage, null, null, null, null, 0, 0l, 0l);
    }

    /**
     * Map Response from event-bus call to REST response
     *
     * @param objectFunction pass io.vertx.core.AsyncResult and future to complete with a Object
     * @param encoder        the Object encoder
     * @return the response chain
     */
    public ExecuteEventbusBasicObjectResponse mapToObjectResponse(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventbusObjectExecutionUtil.mapToObjectResponse(methodId, targetId, message, objectFunction, requestOptions, vertx, t, errorMethodHandler,
                requestmessage, null, encoder, null, null, null, 0, 0l, 0l);
    }


}
