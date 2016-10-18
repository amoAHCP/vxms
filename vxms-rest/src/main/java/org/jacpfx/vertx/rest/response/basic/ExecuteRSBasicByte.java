package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.vertx.rest.util.ResponseUtil;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.*;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByte {
    protected final Vertx vertx;
    protected final Throwable t;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final ThrowableFutureConsumer<byte[]> byteConsumer;
    protected final ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond;
    protected final ExecuteEventBusByteCall excecuteEventBusAndReply;
    protected final Encoder encoder;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final long timeout;

    public ExecuteRSBasicByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<byte[]> byteConsumer, ExecuteEventBusByteCall excecuteEventBusAndReply, Encoder encoder,
                              Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int retryCount,long timeout) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.byteConsumer = byteConsumer;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
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
        new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, status.code(), retryCount,timeout).execute();
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
        new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers,contentType), byteConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, status.code(), retryCount,timeout).execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers,contentType), byteConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, httpStatusCode, retryCount,timeout).execute();
    }



    /**
     * Execute the reply chain
     */
    public void execute() {
        vertx.runOnContext(action -> {
            // excecuteEventBusAndReply & stringSupplier never non null at the same time
            ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount,timeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });


            ofNullable(byteConsumer).
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

    protected void respond(byte[] result, int statuscode) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            ResponseUtil.updateHeaderAndStatuscode(headers,statuscode, response);
            if (result != null) {
                response.end(Buffer.buffer(result));
            } else {
                response.end();
            }
        }
    }



    protected void respond(String result, int statuscode) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            ResponseUtil.updateHeaderAndStatuscode(headers,statuscode, response);
            if (result != null) {
                response.end(result);
            } else {
                response.end();
            }
        }
    }

    protected void respond(byte[] result) {
        respond(result,httpStatusCode);
    }




}
