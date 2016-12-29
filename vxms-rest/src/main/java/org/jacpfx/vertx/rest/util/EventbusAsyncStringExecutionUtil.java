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
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncStringExecutionUtil {


    public static ExecuteRSStringResponse mapToStringResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction, Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                              RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<String> _stringSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                              ThrowableFunction<Throwable, String> _onFailureRespond, int _httpStatusCode, int _httpErrorCode, int _retryCount, long _timeout, long _delay, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                              context, headers,
                                                                              encoder, errorHandler, onFailureRespond,
                                                                              httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout) ->
                sendMessageAndSupplyStringHandler(_methodId, _id, _message, _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);

        return new ExecuteRSStringResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier,
                excecuteAsyncEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyStringHandler(String methodId, String id, Object message,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                          Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                          ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {

        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, null);
        } else {
            executeStateful(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }

    }

    private static void executeStateful(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction,
                                        DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                        ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {

        executeLocked(((lock, counter) ->
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == 0) {
                        executeInitialState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock, counter);
                    } else if (currentVal > 0) {
                        executeDefaultState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);
                    } else {
                        executeErrorState(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);
                    }
                })), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    private static void executeInitialState(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                            Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                            ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                            Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler -> executeDefaultState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler,
                context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, lock));
    }

    private static void executeDefaultState(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                            Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                            ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                            Lock lock) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(methodId, id, message, deliveryOptions, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode, httpErrorCode,
                                        retryCount, timeout, delay, circuitBreakerTimeout, event));
    }


    private static void executeErrorState(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                          Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                          Lock lock) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId, vertx, errorMethodHandler, context, headers,
                encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount,
                timeout, delay, circuitBreakerTimeout, lock, cause);
    }

    private static void createStringSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions deliveryOptions,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                       Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {

        final ThrowableSupplier<String> stringSupplier = createStringSupplier(methodId, id, message, deliveryOptions, stringFunction,
                vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event);
        if (circuitBreakerTimeout == 0l) {
            statelessExecution(methodId, id, message, deliveryOptions, stringFunction, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event, stringSupplier);
        } else {
            statefulExecution(methodId, id, message, deliveryOptions, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event, stringSupplier);
        }


    }

    private static void statelessExecution(String methodId, String id, Object message, DeliveryOptions deliveryOptions, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction,
                                           Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                           Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, String> onFailureRespond,
                                           int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, ThrowableSupplier<String> stringSupplier) {
        if (event.succeeded() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSStringResponse(methodId, vertx, event.cause(), errorMethodHandler, context, headers, stringSupplier, null,
                    encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryStringOperation(methodId, id, message, deliveryOptions, stringFunction, vertx, event.cause(), errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }
    }

    private static void statefulExecution(String methodId, String id, Object message, DeliveryOptions deliveryOptions, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction,
                                          Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                          Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, String> onFailureRespond,
                                          int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, ThrowableSupplier<String> stringSupplier) {
        if (event.succeeded()) {
            new ExecuteRSStringResponse(methodId, vertx, t, errorMethodHandler, context, headers, stringSupplier, null,
                    encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else {
            statefulErrorHandling(methodId, id, message, stringFunction, deliveryOptions,
                    vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, event);
        }
    }


    private static void statefulErrorHandling(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction,
                                              DeliveryOptions deliveryOptions, Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                              ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {

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
                            retryStringOperation(methodId, id, message, deliveryOptions, stringFunction, vertx,
                                    cause, errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
                        }
                    } else {
                        final Throwable cause = valHandler.cause();
                        handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay,
                                circuitBreakerTimeout, lock, cause);
                    }
                }), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);


    }


    private static void openCircuitAndHandleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                  ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, Lock lock, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
        counter.addAndGet(-1l, val -> {
            final Throwable cause = event.cause();
            handleError(methodId, vertx, errorMethodHandler, context, headers,
                    encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount,
                    timeout, delay, circuitBreakerTimeout, lock, cause);

        });
    }

    private static void handleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                    ThrowableFunction<Throwable, String> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        ThrowableSupplier<String> failConsumer = () -> {
            assert cause != null;
            throw cause;
        };
        new ExecuteRSStringResponse(methodId, vertx, cause, errorMethodHandler, context, headers, failConsumer, null,
                encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
    }


    private static void retryStringOperation(String methodId, String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                             Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, String> errorHandlerString,
                                             int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        ResponseUtil.handleError(errorHandler, t);
        mapToStringResponse(methodId, id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, null, encoder,
                errorHandler, errorHandlerString, httpStatusCode, httpErrorCode, retryCount - 1, timeout, delay, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableSupplier<String> createStringSupplier(String methodId, String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                                  Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                  Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, String> errorHandlerString, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return () -> {
            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringOperation(methodId, id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
                } else {
                    throw event.cause();
                }
            } else {
                resp = stringFunction.apply(event);
            }
            return resp;
        };
    }

    private static void executeLocked(LockedConsumer consumer, String _methodId, Vertx vertx, Consumer<Throwable> errorHandler,
                                      ThrowableFunction<Throwable, String> onFailureRespond, Consumer<Throwable> errorMethodHandler,
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
