package org.jacpfx.vertx.event.eventbus.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusByteResponse;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusObjectResponse;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusStringResponse;
import org.jacpfx.vertx.event.util.EventbusByteExecutionBlockingUtil;
import org.jacpfx.vertx.event.util.EventbusObjectExecutionBlockingUtil;
import org.jacpfx.vertx.event.util.EventbusStringExecutionBlockingUtil;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventbusBridgeBlockingResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> requestmessage;
    private final String targetId;
    private final Object message;
    private final DeliveryOptions options;


    public EventbusBridgeBlockingResponse(String methodId, Message<Object> requestmessage, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, String targetId, Object message, DeliveryOptions options) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.requestmessage = requestmessage;
        this.targetId = targetId;
        this.message = message;
        this.options = options;
    }


    public ExecuteEventbusStringResponse mapToStringResponse(ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
        return EventbusStringExecutionBlockingUtil.mapToStringResponse(methodId,
                targetId,
                message,
                stringFunction,
                options,
                vertx, t,
                errorMethodHandler,
                null,
                null,
                null,
                null,
                null,
                0, 0l,
                0l, 0l);
    }

    public ExecuteEventbusByteResponse mapToByteResponse(ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventbusByteExecutionBlockingUtil.mapToByteResponse(methodId,
                targetId,
                message,
                byteFunction,
                options,
                vertx, t,
                errorMethodHandler,
                null,
                null,
                null,
                null,
                null,
                0, 0l,
                0l, 0l);
    }

    public ExecuteEventbusObjectResponse mapToObjectResponse(ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventbusObjectExecutionBlockingUtil.mapToObjectResponse(methodId, targetId,
                message,
                objectFunction,
                options,
                vertx, t,
                errorMethodHandler,
                null,
                null, encoder,
                null,
                null,
                null,
                0, 0l,
                0l, 0l);
    }


}
