package org.jacpfx.vertx.event.response;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicByteResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicObjectResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicStringResponse;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API to define a Task and to reply the request with the output of your task.
 */
public class EventbusResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> message;

    /**
     * The constructor to pass all needed members
     *
     * @param methodId           the method identifier
     * @param message            the event-bus message to respond to
     * @param vertx              the vertx instance
     * @param failure            the failure thrown while task execution
     * @param errorMethodHandler the error handler
     */
    public EventbusResponse(String methodId, Message<Object> message, Vertx vertx, Throwable failure, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.message = message;
    }

    /**
     * Switch to blocking mode
     *
     * @return {@link EventbusResponseBlocking}
     */
    public EventbusResponseBlocking blocking() {
        return new EventbusResponseBlocking(methodId, message, vertx, failure, errorMethodHandler);
    }

    /**
     * Returns a byte array to the target type
     *
     * @param byteConsumer consumes a io.vertx.core.Future to compleate with a byte response
     * @return {@link ExecuteEventbusBasicByteResponse}
     */
    public ExecuteEventbusBasicByteResponse byteResponse(ThrowableFutureConsumer<byte[]> byteConsumer) {
        return new ExecuteEventbusBasicByteResponse(methodId, vertx, failure, errorMethodHandler, message, byteConsumer, null, null, null, null, 0, 0l, 0l);
    }

    /**
     * Returns a String to the target type
     *
     * @param stringConsumer consumes a io.vertx.core.Future to compleate with a String response
     * @return {@link ExecuteEventbusBasicStringResponse}
     */
    public ExecuteEventbusBasicStringResponse stringResponse(ThrowableFutureConsumer<String> stringConsumer) {
        return new ExecuteEventbusBasicStringResponse(methodId, vertx, failure, errorMethodHandler, message, stringConsumer, null, null, null, null, 0, 0l, 0l);
    }

    /**
     * Returns a Serializable to the target type
     *
     * @param objectConsumer consumes a io.vertx.core.Future to compleate with a Serialized Object response
     * @return {@link ExecuteEventbusBasicObjectResponse}
     */
    public ExecuteEventbusBasicObjectResponse objectResponse(ThrowableFutureConsumer<Serializable> objectConsumer, Encoder encoder) {
        return new ExecuteEventbusBasicObjectResponse(methodId, vertx, failure, errorMethodHandler, message, objectConsumer, null, encoder, null, null, null, 0, 0l, 0l);
    }


}
