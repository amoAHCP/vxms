package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicString;
import org.jacpfx.vertx.rest.util.ResponseAsyncUtil;
import org.jacpfx.vertx.rest.util.ResponseUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSString extends ExecuteRSBasicString {
    protected final long delay;
    protected final ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final Function<Throwable, String> onFailureRespond;

    public ExecuteRSString(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply, Encoder encoder,
                           Consumer<Throwable> errorHandler, Function<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, null, null, encoder, errorHandler, null, httpStatusCode, retryCount, timeout);
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
        this.stringSupplier = stringSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId,vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, delay);
        lastStep.execute();
    }

    @Override
    /**
     * Execute the reply chain with given http status code and content-type
     *
     * @param status,     the http status code
     * @param contentType , the html content-type
     */
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId,vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType), stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, delay);
        lastStep.execute();
    }


    @Override
    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        final ExecuteRSString lastStep = new ExecuteRSString(methodId,vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType), stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
        lastStep.execute();
    }

    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        Optional.ofNullable(stringSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler ->
                                            ResponseAsyncUtil.executeRetryAndCatchAsync(supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx, retry, timeout, delay),
                                    false,
                                    (Handler<AsyncResult<String>>) value -> {
                                        if (value.succeeded()) {
                                            respond(value.result());
                                            checkAndCloseResponse(retry);
                                        } else {
                                            //respond(value.cause().getMessage(),HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                                        }
                                        //  checkAndCloseResponse(retry);
                                    });
                        }

                );


    }

}
