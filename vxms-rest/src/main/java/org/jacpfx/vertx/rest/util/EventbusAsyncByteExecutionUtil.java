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
import org.jacpfx.common.*;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncByteExecutionUtil {

    public static ExecuteRSByteResponse mapToByteResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction, Vertx _vertx, Throwable _t,
                                                          Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                          ThrowableSupplier<byte[]> _byteSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                          ThrowableFunction<Throwable, byte[]> _errorHandlerByte, int _httpStatusCode, int _httpErrorCode, int _retryCount, long _timeout, long _delay, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusByteCallAsync excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                       context, headers,
                                                                       encoder, errorHandler, errorHandlerByte,
                                                                       httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout) ->
                sendMessageAndSupplyByteHandler(_methodId, _id, _message,
                        _byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers,
                        encoder, errorHandler, errorHandlerByte, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);


        return new ExecuteRSByteResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _byteSupplier,
                excecuteEventBusAndReply, _encoder, _errorHandler, _errorHandlerByte, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyByteHandler(String methodId, String id, Object message,
                                                        ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions,
                                                        Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                        Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode,
                                                        int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {

        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context,
                    headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, null);
        } else {
            executeStateful(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }
    }

    private static void executeStateful(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                        DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                        ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {

        executeLocked(((lock, counter) ->
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == 0) {
                        executeInitialState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock, counter);
                    } else if (currentVal > 0) {
                        executeDefaultState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);
                    } else {
                        executeErrorState(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);
                    }
                })), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }


    private static void executeInitialState(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                            Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond,
                                            int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler ->
                executeDefaultState(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context,
                        headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock));
    }

    private static void executeDefaultState(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                            Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond,
                                            int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock) {

        Optional.ofNullable(lock).ifPresent(Lock::release);
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createByteSupplierAndExecute(methodId, id, message, deliveryOptions, byteFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,httpErrorCode,
                                        retryCount, timeout, delay, circuitBreakerTimeout, event));
    }

    private static void executeErrorState(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                          Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                          Lock lock) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId, vertx, errorMethodHandler, context, headers,
                encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount,
                timeout, delay, circuitBreakerTimeout, lock, cause);
    }

    private static void createByteSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions deliveryOptions, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                     Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<byte[]> byteSupplier = createByteSupplier(methodId, id, message, deliveryOptions, byteFunction, vertx, t, errorMethodHandler,
                context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event);

        if (circuitBreakerTimeout == 0l) {
            statelessExecution(methodId, id, message, deliveryOptions, byteFunction, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode,httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event, byteSupplier);
        } else {
            statefulExecution(methodId, id, message, byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event, byteSupplier);
        }
    }

    private static void statelessExecution(String methodId, String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                           Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                           Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode,int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, ThrowableSupplier<byte[]> byteSupplier) {
        if (event.succeeded() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSByteResponse(methodId, vertx, event.cause(), errorMethodHandler, context, headers, byteSupplier, null, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryByteOperation(methodId, id, message, options, byteFunction, vertx, event.cause(), errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }
    }

    private static void statefulExecution(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions,
                                          Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode,int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                          AsyncResult<Message<Object>> event, ThrowableSupplier<byte[]> byteSupplier) {
        if (event.succeeded()) {
            new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, null, encoder,
                    errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else {
            statefulErrorHandling(methodId, id, message, deliveryOptions, byteFunction,
                    vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event);
        }
    }

    private static void statefulErrorHandling(String methodId, String id, Object message, DeliveryOptions options,
                                              ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                              Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond,
                                              int httpStatusCode,int httpErrorCode,  int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {

        executeLocked((lock, counter) ->
                counter.decrementAndGet(valHandler -> {
                    if (valHandler.succeeded()) {
                        long count = valHandler.result();
                        if (count <= 0) {
                            openCircuitAndHandleError(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event, lock, counter);
                        } else {
                            lock.release();
                            final Throwable cause = event.cause();
                            retryByteOperation(methodId, id, message, options, byteFunction, vertx,
                                    cause, errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode,httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
                        }
                    } else {
                        final Throwable cause = valHandler.cause();
                        handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode,retryCount, timeout, delay,
                                circuitBreakerTimeout, lock, cause);
                    }
                }), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode,httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);


    }

    private static void openCircuitAndHandleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                  Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout,
                                                  long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, Lock lock, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
        counter.addAndGet(-1l, val -> {
            final Throwable cause = event.cause();
            handleError(methodId, vertx, errorMethodHandler, context, headers,
                    encoder, errorHandler, onFailureRespond, httpStatusCode,httpErrorCode, retryCount,
                    timeout, delay, circuitBreakerTimeout, lock, cause);

        });
    }

    private static void handleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                    ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        ThrowableSupplier<byte[]> failConsumer = () -> {
            assert cause != null;
            throw cause;
        };
        new ExecuteRSByteResponse(methodId, vertx, cause, errorMethodHandler, context, headers, failConsumer, null,
                encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
    }

    private static void retryByteOperation(String methodId, String id, Object message, DeliveryOptions options,
                                           ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler,
                                           RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                           ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        ResponseUtil.handleError(errorHandler, t);
        mapToByteResponse(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler,
                context, headers, null, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode,retryCount - 1, timeout, delay, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableSupplier<byte[]> createByteSupplier(String methodId, String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                                Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return () -> {
            byte[] resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryByteOperation(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                            onFailureRespond, httpStatusCode,httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
                } else {
                    throw event.cause();
                }
            } else {
                resp = byteFunction.apply(event);
            }

            return resp;
        };
    }

    private static void executeLocked(LockedConsumer consumer, String _methodId, Vertx vertx, Consumer<Throwable> errorHandler,
                                      ThrowableFunction<Throwable, byte[]> onFailureRespond, Consumer<Throwable> errorMethodHandler,
                                      RoutingContext context, Map<String, String> headers, Encoder encoder,
                                      int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
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
                                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(_methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, null, cause);
            }

        });
    }


    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
    }


}
