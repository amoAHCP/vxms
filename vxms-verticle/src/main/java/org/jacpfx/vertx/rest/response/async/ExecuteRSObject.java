package org.jacpfx.vertx.rest.response.async;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObject;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSObject extends ExecuteRSBasicObject {
    protected final long delay;
    protected final long timeout;


    public ExecuteRSObject(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<Serializable> objectSupplier, ExecuteEventBusObjectCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSObject lastStep = new ExecuteRSObject(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerObject, status.code(), retryCount, delay, timeout);
        lastStep.execute();
    }

    /**
     * Execute the reply chain with given http status code and content-type
     *
     * @param status,     the http status code
     * @param contentType , the html content-type
     */
    @Override
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        final ExecuteRSObject lastStep = new ExecuteRSObject(vertx, t, errorMethodHandler, context, headerMap, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerObject, status.code(), retryCount, delay, timeout);
        lastStep.execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    @Override
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        final ExecuteRSObject lastStep = new ExecuteRSObject(vertx, t, errorMethodHandler, context, headerMap, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, delay, timeout);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(objectSupplier).
                ifPresent(supplier ->
                        this.vertx.executeBlocking(handler ->
                                        RESTExecutionUtil.executeRetryAndCatchAsync(supplier, handler, errorHandler, errorHandlerObject, errorMethodHandler, vertx, retryCount, timeout, delay),
                                false,
                                (Handler<AsyncResult<Serializable>>) value -> {
                                    if (value.failed()) return;
                                    repond(value.result());
                                })
                );

    }


}
