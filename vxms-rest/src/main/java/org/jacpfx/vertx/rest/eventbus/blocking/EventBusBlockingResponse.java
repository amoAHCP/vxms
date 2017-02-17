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
 * Represents the start of a blocking execution chain
 */
public class EventBusBlockingResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String targetId;
    private final Object message;
    private final DeliveryOptions options;


    /**
     * Pass all parameters to execute the chain
     *
     * @param methodId           the method identifier
     * @param vertx              the vertx instance
     * @param failure            the vertx instance
     * @param errorMethodHandler the error-method handler
     * @param context            the vertx routing context
     * @param targetId           the event-bus message target-targetId
     * @param message            the event-bus message
     * @param options            the event-bus delivery options
     */
    public EventBusBlockingResponse(String methodId,
                                    Vertx vertx,
                                    Throwable failure,
                                    Consumer<Throwable> errorMethodHandler,
                                    RoutingContext context,
                                    String targetId,
                                    Object message,
                                    DeliveryOptions options) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.targetId = targetId;
        this.message = message;
        this.options = options;
    }


    /**
     * Maps the event-bus response to a String response for the REST request
     *
     * @param stringFunction the function, that takes the response message from the event bus and that maps it to a valid response for the REST request
     * @return the execution chain {@link ExecuteRSStringResponse}
     */
    public ExecuteRSStringResponse mapToStringResponse(ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
        return EventBusStringExecutionBlockingUtil.mapToStringResponse(methodId,
                targetId,
                message,
                options,
                stringFunction,
                vertx,
                failure,
                errorMethodHandler,
                context,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0l,
                0l,
                0l);
    }

    /**
     * Maps the event-bus response to a byte response for the REST request
     *
     * @param byteFunction the function, that takes the response message from the event bus and that maps it to a valid response for the REST request
     * @return the execution chain {@link ExecuteRSByteResponse}
     */
    public ExecuteRSByteResponse mapToByteResponse(ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return EventBusByteExecutionBlockingUtil.mapToByteResponse(methodId,
                targetId,
                message,
                options,
                byteFunction,
                vertx,
                failure,
                errorMethodHandler,
                context,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0l,
                0l,
                0l);
    }

    /**
     * Maps the event-bus response to a byte response for the REST request
     *
     * @param objectFunction the function, that takes the response message from the event bus and that maps it to a valid response for the REST request
     * @param encoder        the encoder to serialize your object response
     * @return the execution chain {@link ExecuteRSByteResponse}
     */
    public ExecuteRSObjectResponse mapToObjectResponse(ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Encoder encoder) {

        return EventBusObjectExecutionBlockingUtil.mapToObjectResponse(methodId,
                targetId,
                message,
                options,
                objectFunction,
                vertx,
                failure,
                errorMethodHandler,
                context,
                null,
                null,
                encoder,
                null,
                null,
                0,
                0,
                0,
                0l,
                0l,
                0l);
    }


}
