package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusByteExecutionUtil {

    public static ExecuteRSBasicByteResponse mapToByteResponse(String _methodId,
                                                               String _id,
                                                               Object _message,
                                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> _byteFunction,
                                                               DeliveryOptions _options,
                                                               Vertx _vertx, Throwable _t,
                                                               Consumer<Throwable> _errorMethodHandler,
                                                               RoutingContext _context,
                                                               Map<String, String> _headers,
                                                               ThrowableFutureConsumer<byte[]> _byteConsumer,
                                                               Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                               ThrowableErrorConsumer<Throwable, byte[]> _onFailureRespond,
                                                               int _httpStatusCode, int _httpErrorCode, int _retryCount,
                                                               long _timeout, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusByteCall excecuteEventBusAndReply = (vertx,
                                                                  t,
                                                                  errorMethodHandler,
                                                                  context, headers,
                                                                  encoder, errorHandler,
                                                                  onFailureRespond,
                                                                  httpStatusCode,
                                                                  httpErrorCode,
                                                                  retryCount,
                                                                  timeout, circuitBreakerTimeout) ->
                sendMessageAndSupplyByteHandler(_methodId, _id, _message, _byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);


        return new ExecuteRSBasicByteResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _byteConsumer, excecuteEventBusAndReply, _encoder, _errorHandler,
                _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyByteHandler(String methodId,
                                                        String id,
                                                        Object message,
                                                        ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                                        DeliveryOptions deliveryOptions,
                                                        Vertx vertx, Throwable t,
                                                        Consumer<Throwable> errorMethodHandler,
                                                        RoutingContext context,
                                                        Map<String, String> headers,
                                                        Encoder encoder, Consumer<Throwable> errorHandler,
                                                        ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                        int httpStatusCode, int httpErrorCode,
                                                        int retryCount, long timeout, long circuitBreakerTimeout) {

        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(methodId,
                    id,
                    message,
                    byteFunction,
                    deliveryOptions,
                    vertx, t,
                    errorMethodHandler, context,
                    headers, encoder,
                    errorHandler, onFailureRespond,
                    httpStatusCode, httpErrorCode,
                    retryCount, timeout, circuitBreakerTimeout, null);
        } else {
            executeStateful(methodId,
                    id, message,
                    byteFunction,
                    deliveryOptions,
                    vertx, t, errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout, circuitBreakerTimeout);
        }
    }

    private static void executeStateful(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                        DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                        ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout) {

        executeLocked(((lock, counter) ->
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == 0) {
                        executeInitialState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, lock, counter);
                    } else if (currentVal > 0) {
                        executeDefaultState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, lock);
                    } else {
                        executeErrorState(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, lock);
                    }
                })), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }


    private static void executeInitialState(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                            Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                            int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout, Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler -> executeDefaultState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context,
                headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, lock));
    }

    private static void executeDefaultState(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                            Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                            int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout, Lock lock) {

        Optional.ofNullable(lock).ifPresent(Lock::release);
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createbyteConsumerAndExecute(methodId, id, message, byteFunction,
                                        deliveryOptions, vertx, t, errorMethodHandler, context, headers,
                                        encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode,
                                        retryCount, timeout, circuitBreakerTimeout, event));
    }

    private static void executeErrorState(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                          Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout,
                                          Lock lock) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId, vertx, errorMethodHandler, context, headers,
                encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount,
                timeout, circuitBreakerTimeout, lock, cause);
    }

    private static void createbyteConsumerAndExecute(String methodId, String id, Object message,
                                                     ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                     Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                     int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<byte[]> byteConsumer = createbyteConsumer(byteFunction, event);

        if (circuitBreakerTimeout == 0l) {
            statelessExecution(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, event, byteConsumer);
        } else {
            statefulExecution(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, event, byteConsumer);
        }

    }


    private static void statelessExecution(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                           DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                           Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                           int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event,
                                           ThrowableFutureConsumer<byte[]> byteConsumer) {
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicByteResponse(methodId, vertx, event.cause(), errorMethodHandler, context, headers, byteConsumer, null, encoder,
                    errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryByteOperation(methodId, id, message, byteFunction, deliveryOptions, vertx, event.cause(), errorMethodHandler, context, headers, encoder,
                    errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
        }
    }

    private static void statefulExecution(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions,
                                          Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout,
                                          AsyncResult<Message<Object>> event, ThrowableFutureConsumer<byte[]> byteConsumer) {
        // TODO check if retry set... otherwise it makes no sense... throw exception
        if (event.succeeded()) {
            new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteConsumer, null, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout,
                    circuitBreakerTimeout).execute();
        } else {
            statefulErrorHandling(methodId, id, message, deliveryOptions, byteFunction,
                    vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, event);
        }
    }

    private static void statefulErrorHandling(String methodId, String id, Object message, DeliveryOptions options,
                                              ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                              Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                              int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {

        executeLocked((lock, counter) ->
                counter.decrementAndGet(valHandler -> {
                    if (valHandler.succeeded()) {
                        long count = valHandler.result();
                        if (count <= 0) {
                            openCircuitAndHandleError(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, event, lock, counter);
                        } else {
                            lock.release();
                            retryByteOperation(methodId, id, message, byteFunction, options, vertx,
                                    event.cause(), errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
                        }
                    } else {
                        final Throwable cause = valHandler.cause();
                        handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout,
                                circuitBreakerTimeout, lock, cause);
                    }
                }), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);


    }

    private static void executeLocked(LockedConsumer consumer, String _methodId, Vertx vertx, Consumer<Throwable> errorHandler,
                                      ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, Consumer<Throwable> errorMethodHandler,
                                      RoutingContext context, Map<String, String> headers, Encoder encoder,
                                      int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(_methodId, 2000, lockHandler -> {
            if (lockHandler.succeeded()) {
                final Lock lock = lockHandler.result();
                sharedData.getCounter(_methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        consumer.execute(lock, resultHandler.result());
                    } else {
                        final Throwable cause = resultHandler.cause();
                        handleError(_methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, lock, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(_methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, null, cause);
            }

        });
    }


    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
    }

    private static void openCircuitAndHandleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                  Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout,
                                                  long circuitBreakerTimeout, AsyncResult<Message<Object>> event, Lock lock, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
        counter.addAndGet(-1l, val -> {
            final Throwable cause = event.cause();
            handleError(methodId, vertx, errorMethodHandler, context, headers,
                    encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount,
                    timeout, circuitBreakerTimeout, lock, cause);

        });
    }

    private static void handleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                    ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout, Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        final ThrowableFutureConsumer<byte[]> failConsumer = (future) -> future.fail(cause);
        new ExecuteRSBasicByteResponse(methodId, vertx, cause, errorMethodHandler, context, headers,
                failConsumer, null, encoder, errorHandler, onFailureRespond,
                httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout).
                execute();
    }

    private static void retryByteOperation(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                           Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                           Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                           int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        ResponseUtil.handleError(errorHandler, t);
        mapToByteResponse(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler,
                context, headers, null, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount - 1, timeout, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableFutureConsumer<byte[]> createbyteConsumer(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, AsyncResult<Message<Object>> event) {
        return (future) -> {

            if (event.failed()) {
                future.fail(event.cause());
            } else {
                byteFunction.accept(event, future);
            }
        };
    }
}
