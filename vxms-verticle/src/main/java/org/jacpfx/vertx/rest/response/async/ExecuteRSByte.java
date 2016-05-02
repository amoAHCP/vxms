package org.jacpfx.vertx.rest.response.async;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByte;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByte extends ExecuteRSBasicByte {
    protected final long timeout;
    protected final long delay;
    protected final ExecuteEventBusByteCallAsync excecuteAsyncEventBusAndReply;

    public ExecuteRSByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                         ThrowableSupplier<byte[]> byteSupplier,ExecuteEventBusByteCallAsync excecuteAsyncEventBusAndReply, Encoder encoder,
                         Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers,byteSupplier, null, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
        this.timeout = timeout;
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSByte lastStep = new ExecuteRSByte(vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply,encoder, errorHandler, errorHandlerByte, status.code(), retryCount, timeout, delay);
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
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSByte lastStep = new ExecuteRSByte(vertx, t, errorMethodHandler, context, headerMap, byteSupplier,excecuteAsyncEventBusAndReply, encoder, errorHandler, errorHandlerByte, status.code(), retryCount, timeout, delay);
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
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSByte lastStep = new ExecuteRSByte(vertx, t, errorMethodHandler, context, headerMap, byteSupplier, excecuteAsyncEventBusAndReply,encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount,timeout,delay);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier ->
                        this.vertx.executeBlocking(handler ->
                                        RESTExecutionUtil.executeRetryAndCatchAsync(supplier, handler, errorHandler, errorHandlerByte, errorMethodHandler, vertx, retryCount, timeout, delay),
                                false,
                                (Handler<AsyncResult<byte[]>>) value -> {
                                    if (value.failed()) return;
                                    repond(value.result());
                                })
                );


    }


}
