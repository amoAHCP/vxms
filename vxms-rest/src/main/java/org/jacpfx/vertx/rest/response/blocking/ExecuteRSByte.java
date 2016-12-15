package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByte;
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
public class ExecuteRSByte extends ExecuteRSBasicByte {
    protected final long delay;
    protected final ExecuteEventBusByteCallAsync excecuteAsyncEventBusAndReply;
    protected final ThrowableSupplier<byte[]> byteSupplier;
    protected final Function<Throwable, byte[]> onFailureRespond;

    public ExecuteRSByte(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                         ThrowableSupplier<byte[]> byteSupplier, ExecuteEventBusByteCallAsync excecuteAsyncEventBusAndReply, Encoder encoder,
                         Consumer<Throwable> errorHandler, Function<Throwable, byte[]> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, null, null, encoder, errorHandler, null, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
        this.delay = delay;
        this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
        this.byteSupplier = byteSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSByte lastStep = new ExecuteRSByte(methodId, vertx, t, errorMethodHandler, context,
                headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, delay, circuitBreakerTimeout);
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
        final ExecuteRSByte lastStep = new ExecuteRSByte(methodId, vertx, t, errorMethodHandler, context,
                ResponseUtil.updateContentType(headers, contentType), byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout, delay, circuitBreakerTimeout);
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
        final ExecuteRSByte lastStep = new ExecuteRSByte(methodId, vertx, t, errorMethodHandler, context,
                ResponseUtil.updateContentType(headers, contentType), byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler -> executeAsync(supplier, retry, handler), false, getAsyncResultHandler(retry));
                        }

                );


    }

    public void executeAsync(ThrowableSupplier<byte[]> supplier, int retry, Future<byte[]> handler) {
        ResponseAsyncUtil.executeRetryAndCatchAsync(methodId,supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx,t, retry, timeout,circuitBreakerTimeout, delay);
    }

    public Handler<AsyncResult<byte[]>> getAsyncResultHandler(int retry) {
        return value -> {
            if (!value.failed()) {
                respond(value.result());
            } else {
                checkAndCloseResponse(retry);
            }

        };
    }


}
