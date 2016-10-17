package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCall;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.rest.util.ResponseUtil;

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
public class ExecuteRSBasicObject {

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

    public ExecuteRSBasicObject(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<Serializable> objectConsumer, ExecuteEventBusObjectCall excecuteEventBusAndReply, Encoder encoder,
                                Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout) {
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
    }


    /**
     * Execute the reply chain with given http status code
     *
     * @param status, the http status code
     */
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSBasicObject lastStep = new ExecuteRSBasicObject(vertx, t, errorMethodHandler, context, headers, objectConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout);
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
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSBasicObject lastStep = new ExecuteRSBasicObject(vertx, t, errorMethodHandler, context, headerMap, objectConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout);
        lastStep.execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSBasicObject lastStep = new ExecuteRSBasicObject(vertx, t, errorMethodHandler, context, headerMap, objectConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
        lastStep.execute();
    }

    protected Map<String, String> updateContentMap(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return headerMap;
    }

    /**
     * Execute the reply chain
     */
    public void execute() {
        vertx.runOnContext(action -> {
            // excecuteEventBusAndReply & stringSupplier never non null at the same time
            Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            Optional.ofNullable(objectConsumer).
                    ifPresent(userOperation -> {
                                int retry = retryCount;
                                ResponseUtil.createResponse(retry, timeout, userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, value -> {
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
            updateHeaderAndStatuscode(statuscode, response);
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
            updateHeaderAndStatuscode(httpStatusCode, response);
            if (result != null) {
                RESTExecutionUtil.encode(result, encoder).ifPresent(value -> RESTExecutionUtil.sendObjectResult(value, context.response()));
            } else {
                response.end();
            }
        }
    }
    private void updateHeaderAndStatuscode(int statuscode, HttpServerResponse response) {
        RESTExecutionUtil.updateResponseHaders(headers, response);
        RESTExecutionUtil.updateResponseStatusCode(statuscode, response);
    }

}
