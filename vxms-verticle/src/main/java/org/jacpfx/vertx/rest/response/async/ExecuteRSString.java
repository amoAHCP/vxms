package org.jacpfx.vertx.rest.response.async;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicString;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;

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
    protected final long timeout;
    protected final ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply;

    public ExecuteRSString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply, Encoder encoder,
                           Consumer<Throwable> errorHandler, Function<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, stringSupplier, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
        this.delay = delay;
        this.timeout = timeout;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSString lastStep = new ExecuteRSString(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, delay);
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
        final Map<String, String> headerMap = updateContentType(contentType);
        final ExecuteRSString lastStep = new ExecuteRSString(vertx, t, errorMethodHandler, context, headerMap, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, delay);
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
        final Map<String, String> headerMap = updateContentType(contentType);
        final ExecuteRSString lastStep = new ExecuteRSString(vertx, t, errorMethodHandler, context, headerMap, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
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
                                            RESTExecutionUtil.executeRetryAndCatchAsync(supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx, retry, timeout, delay),
                                    false,
                                    (Handler<AsyncResult<String>>) value -> {
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
