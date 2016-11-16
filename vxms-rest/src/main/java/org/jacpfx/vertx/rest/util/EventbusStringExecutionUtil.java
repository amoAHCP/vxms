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
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusStringExecutionUtil {


    public static ExecuteRSBasicStringResponse mapToStringResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                                   ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> _stringFunction, Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                                   ThrowableFutureConsumer<String> _stringConsumer, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                                   ThrowableErrorConsumer<Throwable, String> _onFailureRespond, int _httpStatusCode, int _retryCount, long _timeout, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler, onFailureRespond,
                                                                    httpStatusCode, retryCount, timeout, circuitBreakerTimeout) ->
                sendMessageAndSupplyStringHandler(_methodId, _id, _message, _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);

        return new ExecuteRSBasicStringResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _stringConsumer, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _retryCount, _timeout, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyStringHandler(String methodId, String id, Object message,
                                                          ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction,
                                                          DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                          Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                          ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, null);
        } else {
            executeStateful(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
        }
    }

    private static void executeStateful(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(methodId, 2000, lockHandler -> {
            if (lockHandler.succeeded()) {
                sharedData.getCounter(methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        resultHandler.result().get(counterHandler -> {
                            long currentVal = counterHandler.result();
                            if (currentVal == 0) {
                                executeInitialState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, lockHandler, resultHandler);
                            } else if (currentVal > 0) {
                                executeDefaultState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, lockHandler);
                            } else {
                                executeErrorState(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, lockHandler);
                            }
                        });
                    } else {
                        final Throwable cause = resultHandler.cause();
                        handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout,
                                circuitBreakerTimeout, lockHandler, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                        errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout,
                        circuitBreakerTimeout, null, cause);

            }
        });
    }

    private static void executeErrorState(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Lock> lockHandler) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId, vertx, errorMethodHandler, context, headers,
                encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount,
                timeout, circuitBreakerTimeout, lockHandler, cause);
    }


    private static void executeInitialState(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Lock> lockHandler, AsyncResult<Counter> resultHandler) {
        resultHandler.result().addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler -> {
            executeDefaultState(methodId, id, message, stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, lockHandler);
        });
    }

    private static void executeDefaultState(String methodId, String id, Object message, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Lock> lockHandler) {
        Optional.ofNullable(lockHandler).ifPresent(lck -> lck.result().release());
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(methodId, id, message, deliveryOptions, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,
                                        retryCount, timeout, circuitBreakerTimeout, event));
    }

    private static void createStringSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions options,
                                                       ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                                       int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<String> stringSupplier = createStringSupplier(stringFunction, event);
        if (circuitBreakerTimeout == 0l) {
            if (event.succeeded() || (event.failed() && retryCount <= 0)) {
                new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers,
                        stringSupplier, null, encoder, errorHandler, onFailureRespond,
                        httpStatusCode, retryCount, timeout, circuitBreakerTimeout).
                        execute();
            } else if (event.failed() && retryCount > 0) {
                // retry operation
                retryStringOperation(methodId, id, message, options, stringFunction, vertx,
                        event.cause(), errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
            }
        } else {
            // TODO check if retry set... otherwise it makes no sense... throw exception
            if (event.succeeded()) {
                new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers,
                        stringSupplier, null, encoder, errorHandler, onFailureRespond,
                        httpStatusCode, retryCount, timeout, circuitBreakerTimeout).
                        execute();
            } else {
                statefulErrorHandling(methodId, id, message, options, stringFunction,
                        vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, event);
            }

        }
    }

    private static void statefulErrorHandling(String methodId, String id, Object message, DeliveryOptions options, ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(methodId, 2000, lockHandler -> {
            if (lockHandler.succeeded()) {
                sharedData.getCounter(methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        final Counter counter = resultHandler.result();
                        counter.decrementAndGet(valHandler -> {
                            if (valHandler.succeeded()) {
                                long count = valHandler.result();
                                if (count <= 0) {
                                    lockAndHandleError(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, event, lockHandler, counter);
                                } else {
                                    lockHandler.result().release();
                                    retryStringOperation(methodId, id, message, options, stringFunction, vertx,
                                            event.cause(), errorMethodHandler, context, headers, encoder, errorHandler,
                                            onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
                                }
                            } else {
                                final Throwable cause = valHandler.cause();
                                handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout,
                                        circuitBreakerTimeout, lockHandler, cause);
                            }
                        });
                    } else {
                        final Throwable cause = resultHandler.cause();
                        handleError(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                                onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, lockHandler, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, lockHandler, cause);
            }
        });
    }

    private static void lockAndHandleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, AsyncResult<Lock> lockHandler, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
        counter.addAndGet(-1l, val -> {
            final Throwable cause = event.cause();
            handleError(methodId, vertx, errorMethodHandler, context, headers,
                    encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount,
                    timeout, circuitBreakerTimeout, lockHandler, cause);

        });
    }

    private static void handleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Lock> lockHandler, Throwable cause) {
        Optional.ofNullable(lockHandler).ifPresent(lck -> lck.result().release());
        final ThrowableFutureConsumer<String> failConsumer = (future) -> future.fail(cause);
        new ExecuteRSBasicStringResponse(methodId, vertx, cause, errorMethodHandler, context, headers,
                failConsumer, null, encoder, errorHandler, onFailureRespond,
                httpStatusCode, retryCount, timeout, circuitBreakerTimeout).
                execute();
    }


    private static void retryStringOperation(String methodId, String id, Object message, DeliveryOptions options,
                                             ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                             Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                             Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                             int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        ResponseUtil.handleError(errorHandler, t);
        mapToStringResponse(methodId, id, message, options, stringFunction, vertx, t,
                errorMethodHandler, context, headers, null, encoder, errorHandler,
                onFailureRespond, httpStatusCode, retryCount - 1, timeout, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableFutureConsumer<String> createStringSupplier(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, AsyncResult<Message<Object>> event) {
        return (future) -> {
            if (event.failed()) {
                future.fail(event.cause());
            } else {
                stringFunction.accept(event, future);
            }
        };
    }


}
