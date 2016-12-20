package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import io.vertx.core.shareddata.Lock;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncObjectExecutionUtil {

    public static ExecuteRSObjectResponse mapToObjectResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> _objectFunction, Vertx _vertx, Throwable _t,
                                                              Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                              ThrowableSupplier<Serializable> _objectSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                              Function<Throwable, Serializable> _errorHandlerObject, int _httpStatusCode, int _retryCount, long _timeout, long _delay, long _circuitBreakerTimeout) {
        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusObjectCallAsync excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                         context, headers,
                                                                         encoder, errorHandler, errorHandlerObject,
                                                                         httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout) ->
                sendMessageAndSupplyObjectHandler(_methodId, _id, _message, _objectFunction, deliveryOptions, vertx, t,
                        errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);


        return new ExecuteRSObjectResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _objectSupplier, excecuteEventBusAndReply,
                _encoder, _errorHandler, _errorHandlerObject, _httpStatusCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyObjectHandler(String methodId, String id, Object message,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, DeliveryOptions deliveryOptions,
                                                          Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                          Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {

        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(methodId, id, message, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                    errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, null);
        } else {
            executeStateful(methodId, id, message, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                    errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }

    }

    private static void executeStateful(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction,
                                        DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                        Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {

        executeLocked(((lock, counter) ->
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == 0) {
                        executeInitialState(methodId, id, message, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, lock, counter);
                    } else if (currentVal > 0) {
                        executeDefaultState(methodId, id, message, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);
                    } else {
                        executeErrorState(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);
                    }
                })), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    private static void executeInitialState(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                            Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> onFailureRespond,
                                            int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler -> {
            executeDefaultState(methodId, id, message, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context,
                    headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, lock);

        });
    }

    private static void executeDefaultState(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction,
                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                            Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                            int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock) {
        Optional.ofNullable(lock).ifPresent(lck -> lck.release());
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createObjectSupplierAndExecute(methodId, id, message, deliveryOptions, objectFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerObject, httpStatusCode,
                                        retryCount, timeout, delay, circuitBreakerTimeout, event));
    }

    private static void executeErrorState(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                          Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                          Lock lock) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId, vertx, errorMethodHandler, context, headers,
                encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount,
                timeout, delay, circuitBreakerTimeout, lock, cause);
    }

    private static void createObjectSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions deliveryOptions,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                                       int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<Serializable> objectSupplier = createObjectSupplier(methodId, id, message, deliveryOptions, objectFunction, vertx, t,
                errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, event);

        if (circuitBreakerTimeout == 0l) {
            statelessExecution(methodId, id, message, deliveryOptions, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, event, objectSupplier);

        } else {
            statefulExecution(methodId, id, message, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, event, objectSupplier);

        }
    }

    private static void statelessExecution(String methodId, String id, Object message, DeliveryOptions deliveryOptions, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, ThrowableSupplier<Serializable> objectSupplier) {
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSObjectResponse(methodId, vertx, t, errorMethodHandler, context, headers, objectSupplier, null,
                    encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryObjectOperation(methodId, id, message, deliveryOptions, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }
    }

    private static void statefulExecution(String methodId, String id, Object message, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, DeliveryOptions deliveryOptions,
                                          Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                          Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout,
                                          AsyncResult<Message<Object>> event, ThrowableSupplier<Serializable> byteSupplier) {
        if (event.succeeded()) {
            new ExecuteRSObjectResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, null, encoder,
                    errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else {
            statefulErrorHandling(methodId, id, message, deliveryOptions, objectFunction,
                    vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                    onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, event);
        }
    }

    private static void statefulErrorHandling(String methodId, String id, Object message, DeliveryOptions options,
                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                              Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> onFailureRespond,
                                              int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {

        executeLocked((lock, counter) ->
                counter.decrementAndGet(valHandler -> {
                    if (valHandler.succeeded()) {
                        long count = valHandler.result();
                        if (count <= 0) {
                            openCircuitAndHandleError(methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, event, lock, counter);
                        } else {
                            lock.release();
                            retryObjectOperation(methodId, id, message, options, objectFunction, vertx,
                                    event.cause(), errorMethodHandler, context, headers, encoder, errorHandler,
                                    onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
                        }
                    } else {
                        final Throwable cause = valHandler.cause();
                        handleError(methodId, vertx, errorMethodHandler, context, headers, encoder,
                                errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay,
                                circuitBreakerTimeout, lock, cause);
                    }
                }), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);


    }

    private static void openCircuitAndHandleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                  Consumer<Throwable> errorHandler, Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout,
                                                  long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event, Lock lock, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
        counter.addAndGet(-1l, val -> {
            final Throwable cause = event.cause();
            handleError(methodId, vertx, errorMethodHandler, context, headers,
                    encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount,
                    timeout, delay, circuitBreakerTimeout, lock, cause);

        });
    }


    private static void handleError(String methodId, Vertx vertx, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                    Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(lck -> lck.release());
        ThrowableSupplier<Serializable> failConsumer = () -> {
            throw cause;
        };
        new ExecuteRSObjectResponse(methodId, vertx, cause, errorMethodHandler, context, headers, failConsumer, null,
                encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
    }

    private static void retryObjectOperation(String methodId, String id, Object message, DeliveryOptions deliveryOptions, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                             Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                             int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        mapToObjectResponse(methodId, id, message, deliveryOptions, objectFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount - 1, timeout, delay, circuitBreakerTimeout).
                execute();
    }

    private static ThrowableSupplier<Serializable> createObjectSupplier(String methodId, String id, Object message, DeliveryOptions deliveryOptions,
                                                                        ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                        Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                                                        int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return () -> {
            Serializable resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryObjectOperation(methodId, id, message, deliveryOptions, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
                } else {
                    throw event.cause();
                }
            } else {
                resp = objectFunction.apply(event);
            }

            return resp;
        };
    }

    private static void executeLocked(LockedConsumer consumer, String _methodId, Vertx vertx, Consumer<Throwable> errorHandler,
                                      Function<Throwable, Serializable> onFailureRespond, Consumer<Throwable> errorMethodHandler,
                                      RoutingContext context, Map<String, String> headers, Encoder encoder,
                                      int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
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
                                onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, lock, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(_methodId, vertx, errorMethodHandler, context, headers, encoder, errorHandler,
                        onFailureRespond, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, null, cause);
            }

        });
    }


    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
    }


}
