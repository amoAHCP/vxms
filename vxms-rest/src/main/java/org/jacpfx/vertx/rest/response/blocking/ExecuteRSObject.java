package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObject;
import org.jacpfx.vertx.rest.util.ResponseAsyncUtil;
import org.jacpfx.vertx.rest.util.ResponseUtil;

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
    protected final ThrowableSupplier<Serializable> objectSupplier;
    protected final ThrowableFunction<Throwable, Serializable> onFailureRespond;

    public ExecuteRSObject(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<Serializable> objectSupplier, ExecuteEventBusObjectCallAsync excecuteEventBusAndReply, Encoder encoder,
                           Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, null, null, encoder, errorHandler, null, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
        this.delay = delay;
        this.timeout = timeout;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.objectSupplier = objectSupplier;
        this.onFailureRespond = onFailureRespond;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSObject lastStep = new ExecuteRSObject(methodId, vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, delay, timeout, circuitBreakerTimeout);
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
        final ExecuteRSObject lastStep = new ExecuteRSObject(methodId, vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType), objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, delay, timeout, circuitBreakerTimeout);
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
        final ExecuteRSObject lastStep = new ExecuteRSObject(methodId, vertx, t, errorMethodHandler, context, ResponseUtil.updateContentType(headers, contentType), objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, delay, timeout, circuitBreakerTimeout);
        lastStep.execute();
    }


    @Override
    public void execute() {
        Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
            try {
                // TODO update executeEventBusAndReply !!
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        Optional.ofNullable(objectSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            this.vertx.executeBlocking(handler -> executeAsync(supplier, retry, handler), false, getAsyncResultHandler(retry));
                        }

                );

    }

    private void executeAsync(ThrowableSupplier<Serializable> supplier, int retry, Future<Serializable> handler) {
        ResponseAsyncUtil.executeRetryAndCatchAsync(methodId,supplier, handler, errorHandler, onFailureRespond, errorMethodHandler, vertx, t, retry, timeout, 0l,delay);
    }

    private Handler<AsyncResult<Serializable>> getAsyncResultHandler(int retry) {
        return value -> {
            if (!value.failed()) {
                respond(value.result());
            } else {
                checkAndCloseResponse(retry);
            }
        };
    }


}
