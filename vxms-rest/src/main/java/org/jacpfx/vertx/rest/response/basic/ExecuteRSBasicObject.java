package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCall;
import org.jacpfx.vertx.rest.util.ResponseUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicObject {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable t;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final ThrowableFutureConsumer<Serializable> objectConsumer;
    protected final ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond;
    protected final ExecuteEventBusObjectCall excecuteEventBusAndReply;
    protected final Encoder encoder;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final long timeout;
    protected final long circuitBreakerTimeout;

    public ExecuteRSBasicObject(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<Serializable> objectConsumer, ExecuteEventBusObjectCall excecuteEventBusAndReply, Encoder encoder,
                                Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.objectConsumer = objectConsumer;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.httpStatusCode = httpStatusCode;
        this.retryCount = retryCount;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.timeout = timeout;
        this.circuitBreakerTimeout = circuitBreakerTimeout;
    }


    /**
     * Execute the reply chain with given http status code
     *
     * @param status, the http status code
     */
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSBasicObject lastStep = new ExecuteRSBasicObject(methodId, vertx, t, errorMethodHandler, context, headers, objectConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, circuitBreakerTimeout);
        lastStep.execute();
    }


    /**
     * Execute the reply chain with given http status code and content-type
     *
     * @param status,     the http status code
     * @param contentType , the html content-type
     */
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        final ExecuteRSBasicObject lastStep = new ExecuteRSBasicObject(methodId, vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType), objectConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, circuitBreakerTimeout);
        lastStep.execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        final ExecuteRSBasicObject lastStep = new ExecuteRSBasicObject(methodId, vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType), objectConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
        lastStep.execute();
    }


    /**
     * Execute the reply chain
     */
    public void execute() {
        vertx.runOnContext(action -> {
            // excecuteEventBusAndReply & stringSupplier never non null at the same time
            ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            ofNullable(objectConsumer).
                    ifPresent(userOperation -> {
                                int retry = retryCount;
                                ResponseUtil.createResponse(methodId, retry, timeout, circuitBreakerTimeout, userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, t, value -> {
                                    if (value.succeeded()) {
                                        respond(value.getResult());
                                    } else {
                                        respond(value.getCause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                                    }
                                    checkAndCloseResponse(retry);
                                });
                            }
                    );
        });
    }

    protected void checkAndCloseResponse(int retry) {
        final HttpServerResponse response = context.response();
        if (retry == 0 && !response.ended()) {
            response.end();
        }
    }

    protected void respond(String result, int statuscode) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            ResponseUtil.updateHeaderAndStatuscode(headers, statuscode, response);
            if (result != null) {
                response.end(result);
            } else {
                response.end();
            }
        }
    }

    protected void respond(Serializable result) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            ResponseUtil.updateHeaderAndStatuscode(headers, httpStatusCode, response);
            if (result != null) {
                ResponseUtil.encode(result, encoder).ifPresent(value -> ResponseUtil.sendObjectResult(value, context.response()));
            } else {
                response.end();
            }
        }
    }


}
