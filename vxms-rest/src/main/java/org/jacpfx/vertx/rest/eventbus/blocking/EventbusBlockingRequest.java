package org.jacpfx.vertx.rest.eventbus.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 * Defines an event-bus request as the beginning of your (blocking) execution chain
 */
public class EventbusBlockingRequest {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;

    /**
     * Pass all members to execute the chain
     *
     * @param methodId           the method identifier
     * @param vertx              the vertx instance
     * @param failure            the vertx instance
     * @param errorMethodHandler the error-method handler
     * @param context            the vertx routing context
     */
    public EventbusBlockingRequest(String methodId,
                                   Vertx vertx,
                                   Throwable failure,
                                   Consumer<Throwable> errorMethodHandler,
                                   RoutingContext context) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
    }


    /**
     * Send message and perform (blocking) task on reply
     *
     * @param targetId the target id to send to
     * @param message  the message to send
     * @return the execution chain {@link EventbusBlockingResponse}
     */
    public EventbusBlockingResponse send(String targetId, Object message) {
        return new EventbusBlockingResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                targetId,
                message,
                null);
    }

    /**
     * Send message and perform (blocking) task on reply
     *
     * @param targetId the target id to send to
     * @param message  the message to send
     * @param options the delivery options for sending the message
     * @return the execution chain {@link EventbusBlockingResponse}
     */
    public EventbusBlockingResponse send(String targetId, Object message, DeliveryOptions options) {
        return new EventbusBlockingResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                targetId,
                message,
                options);
    }
}
