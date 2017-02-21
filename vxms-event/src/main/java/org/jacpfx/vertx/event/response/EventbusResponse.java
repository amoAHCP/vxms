package org.jacpfx.vertx.event.response;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.response.basic.ExecuteRSBasicByteResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteRSBasicObjectResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteRSBasicStringResponse;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class EventbusResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> message;

    public EventbusResponse(String methodId, Message<Object> message, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.message = message;
    }

    /**
     * Switch to blocking mode
     *
     * @return @see{org.jacpfx.vertx.rest.response.RSAsyncResponse}
     */
    public EventbusResponseBlocking blocking() {
        return new EventbusResponseBlocking(methodId, vertx, t, errorMethodHandler, message);
    }

    /**
     * Returns a byte array to the target type
     *
     * @param byteConsumer consumes a io.vertx.core.Future to compleate with a byte response
     * @return @see{org.jacpfx.vertx.rest.createResponse.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicByteResponse byteResponse(ThrowableFutureConsumer<byte[]> byteConsumer) {
        return new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, message,  byteConsumer, null, null, null, null, 0, 0l, 0l);
    }

    /**
     * Returns a String to the target type
     *
     * @param stringConsumer consumes a io.vertx.core.Future to compleate with a String response
     * @return @see{org.jacpfx.vertx.rest.createResponse.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicStringResponse stringResponse(ThrowableFutureConsumer<String> stringConsumer) {
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, message, stringConsumer, null, null, null, null, 0,  0l, 0l);
    }

    /**
     * Returns a Serializable to the target type
     *
     * @param objectConsumer consumes a io.vertx.core.Future to compleate with a Serialized Object response
     * @return @see{org.jacpfx.vertx.rest.createResponse.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicObjectResponse objectResponse(ThrowableFutureConsumer<Serializable> objectConsumer, Encoder encoder) {
        return new ExecuteRSBasicObjectResponse(methodId, vertx, t, errorMethodHandler, message,  objectConsumer, null, encoder, null, null,null, 0, 0l, 0l);
    }



}
