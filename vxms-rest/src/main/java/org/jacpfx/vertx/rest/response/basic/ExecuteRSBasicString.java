package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.util.ResponseUtil;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicString {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final ThrowableFutureConsumer<String> stringConsumer;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final ThrowableErrorConsumer<Throwable, String> onFailureRespond;
    protected final ExecuteEventBusStringCall excecuteEventBusAndReply;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final long timeout;
    protected final long circuitBreakerTimeout;


    public ExecuteRSBasicString(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<String> stringConsumer, ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder,
                                Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.stringConsumer = stringConsumer;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
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
        new ExecuteRSBasicString(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer,
                excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, circuitBreakerTimeout).execute();
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
        new ExecuteRSBasicString(methodId, vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType),
                stringConsumer, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, status.code(), retryCount, timeout, circuitBreakerTimeout).execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        new ExecuteRSBasicString(methodId, vertx, t, errorMethodHandler, context,
                ResponseUtil.updateContentType(headers, contentType), stringConsumer, excecuteEventBusAndReply,
                encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout).execute();
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

            ofNullable(stringConsumer).
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

    protected void respond(String result) {
        respond(result, httpStatusCode);
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


}
