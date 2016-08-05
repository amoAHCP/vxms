package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObject;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;

import java.io.Serializable;
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
    protected final ExecuteEventBusObjectCallAsync excecuteEventBusAndReply;

    public ExecuteRSObject(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<Serializable> objectSupplier, ExecuteEventBusObjectCallAsync excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, objectSupplier, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
        this.delay = delay;
        this.timeout = timeout;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSObject lastStep = new ExecuteRSObject(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, delay, timeout);
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
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSObject lastStep = new ExecuteRSObject(vertx, t, errorMethodHandler, context, headerMap, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, delay, timeout);
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
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSObject lastStep = new ExecuteRSObject(vertx, t, errorMethodHandler, context, headerMap, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, delay, timeout);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
            } catch (Exception e) {
                System.out.println("EXCEPTION ::::::");
                e.printStackTrace();
            }

        });

        Optional.ofNullable(objectSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler ->
                                            RESTExecutionUtil.executeRetryAndCatchAsync(supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx, retry, timeout, delay),
                                    false,
                                    (Handler<AsyncResult<Serializable>>) value -> {
                                        if (!value.failed()) {
                                            repond(value.result());
                                        } else {
                                            checkAndCloseResponse(retry);
                                        }
                                    });
                        }

                );

    }


}
