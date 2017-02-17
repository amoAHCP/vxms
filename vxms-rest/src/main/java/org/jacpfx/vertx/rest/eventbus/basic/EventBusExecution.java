package org.jacpfx.vertx.rest.eventbus.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
import org.jacpfx.vertx.rest.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vertx.rest.interfaces.basic.RetryExecutor;
import org.jacpfx.vertx.rest.response.basic.ResponseExecution;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Handles event-bus call and non-blocking execution of the message to create a rest response
 */
public class EventBusExecution {


    public static final long LOCK_VALUE = -1l;
    public static final int DEFAULT_LOCK_TIMEOUT = 2000;
    public static final long DEFAULT_VALUE = 0l;
    /**
     * Send event-bus message and process the result in the passed function for the execution chain
     *
     * @param methodId              the method identifier
     * @param targetId              the event-bus target id
     * @param message               the message to send
     * @param function              the function to process the result message
     * @param deliveryOptions       the event-bus delivery options
     * @param vertx                 the vertx instance
     * @param failure               the failure thrown while task execution
     * @param errorMethodHandler    the error-method handler
     * @param context               the vertx routing context
     * @param headers               the headers to pass to the response
     * @param encoder               the encoder to encode your objects
     * @param errorHandler          the error handler
     * @param onFailureRespond      the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode        the http status code to set for response
     * @param httpErrorCode         the http error code to set in case of failure handling
     * @param retryCount            the amount of retries before failure execution is triggered
     * @param timeout               the amount of time before the execution will be aborted
     * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
     * @param executor              the typed executor to process the chain
     * @param retryExecutor         the typed retryExecutor executor of the chain
     * @param <T>                   the type of response
     */
    public static <T> void sendMessageAndSupplyHandler(String methodId,
                                                       String targetId,
                                                       Object message,
                                                       ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function,
                                                       DeliveryOptions deliveryOptions,
                                                       Vertx vertx,
                                                       Throwable failure,
                                                       Consumer<Throwable> errorMethodHandler,
                                                       RoutingContext context,
                                                       Map<String, String> headers,
                                                       Encoder encoder,
                                                       Consumer<Throwable> errorHandler,
                                                       ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                       int httpStatusCode, int httpErrorCode,
                                                       int retryCount, long timeout,
                                                       long circuitBreakerTimeout,
                                                       RecursiveExecutor executor, RetryExecutor retryExecutor) {
        if (circuitBreakerTimeout == DEFAULT_VALUE) {
            executeDefaultState(methodId,
                    targetId, message,
                    function,
                    deliveryOptions,
                    vertx, failure,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount, timeout,
                    circuitBreakerTimeout,
                    executor, retryExecutor, null);
        } else {
            executeStateful(methodId,
                    targetId, message,
                    function,
                    deliveryOptions,
                    vertx, failure,
                    errorMethodHandler,
                    context,
                    headers,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout,
                    circuitBreakerTimeout,
                    executor, retryExecutor);
        }
    }

    private static <T> void executeStateful(String methodId,
                                            String id, Object message,
                                            ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function,
                                            DeliveryOptions deliveryOptions,
                                            Vertx vertx, Throwable t,
                                            Consumer<Throwable> errorMethodHandler,
                                            RoutingContext context,
                                            Map<String, String> headers,
                                            Encoder encoder,
                                            Consumer<Throwable> errorHandler,
                                            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                            int httpStatusCode, int httpErrorCode,
                                            int retryCount, long timeout,
                                            long circuitBreakerTimeout,
                                            RecursiveExecutor executor,
                                            RetryExecutor retry) {

        executeLocked(((lock, counter) ->
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == DEFAULT_VALUE) {
                        executeInitialState(methodId,
                                id, message,
                                function,
                                deliveryOptions,
                                vertx, t,
                                errorMethodHandler,
                                context, headers,
                                encoder, errorHandler,
                                onFailureRespond,
                                httpStatusCode,
                                httpErrorCode,
                                retryCount,
                                timeout,
                                circuitBreakerTimeout,
                                executor, retry, lock, counter);
                    } else if (currentVal > 0) {
                        executeDefaultState(methodId,
                                id, message,
                                function,
                                deliveryOptions,
                                vertx, t,
                                errorMethodHandler,
                                context, headers,
                                encoder, errorHandler,
                                onFailureRespond,
                                httpStatusCode,
                                httpErrorCode,
                                retryCount,
                                timeout,
                                circuitBreakerTimeout,
                                executor, retry, lock);
                    } else {
                        executeErrorState(methodId,
                                vertx,
                                errorMethodHandler,
                                context,
                                headers,
                                encoder,
                                errorHandler,
                                onFailureRespond,
                                httpStatusCode,
                                httpErrorCode,
                                retryCount,
                                timeout,
                                circuitBreakerTimeout,
                                executor, lock);
                    }
                })), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, executor);
    }

    private static <T> void executeInitialState(String methodId,
                                                String id,
                                                Object message,
                                                ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                                DeliveryOptions deliveryOptions,
                                                Vertx vertx, Throwable t,
                                                Consumer<Throwable> errorMethodHandler,
                                                RoutingContext context,
                                                Map<String, String> headers,
                                                Encoder encoder,
                                                Consumer<Throwable> errorHandler,
                                                ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                int httpStatusCode, int httpErrorCode,
                                                int retryCount, long timeout,
                                                long circuitBreakerTimeout,
                                                RecursiveExecutor executor,
                                                RetryExecutor retry,
                                                Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler ->
                executeDefaultState(methodId,
                        id, message,
                        stringFunction,
                        deliveryOptions,
                        vertx, t,
                        errorMethodHandler,
                        context,
                        headers,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount, timeout,
                        circuitBreakerTimeout,
                        executor, retry, lock));
    }

    private static <T> void executeDefaultState(String methodId,
                                                String id, Object message,
                                                ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                                DeliveryOptions deliveryOptions,
                                                Vertx vertx, Throwable t,
                                                Consumer<Throwable> errorMethodHandler,
                                                RoutingContext context,
                                                Map<String, String> headers,
                                                Encoder encoder,
                                                Consumer<Throwable> errorHandler,
                                                ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                int httpStatusCode, int httpErrorCode,
                                                int retryCount, long timeout,
                                                long circuitBreakerTimeout,
                                                RecursiveExecutor executor,
                                                RetryExecutor retry,
                                                Lock lock) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(methodId,
                                        id, message,
                                        stringFunction,
                                        deliveryOptions,
                                        vertx, t,
                                        errorMethodHandler,
                                        context,
                                        headers,
                                        encoder,
                                        errorHandler,
                                        onFailureRespond,
                                        httpStatusCode,
                                        httpErrorCode,
                                        retryCount, timeout,
                                        circuitBreakerTimeout,
                                        executor, retry, event));
    }

    private static <T> void executeErrorState(String methodId,
                                              Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler,
                                              RoutingContext context,
                                              Map<String, String> headers,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                              int httpStatusCode, int httpErrorCode,
                                              int retryCount, long timeout,
                                              long circuitBreakerTimeout,
                                              RecursiveExecutor executor,
                                              Lock lock) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId,
                vertx,
                errorMethodHandler,
                context,
                headers,
                encoder,
                errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount,
                timeout,
                circuitBreakerTimeout,
                executor,
                lock, cause);
    }


    private static <T> void createStringSupplierAndExecute(String methodId,
                                                           String id, Object message,
                                                           ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                                           DeliveryOptions deliveryOptions,
                                                           Vertx vertx, Throwable t,
                                                           Consumer<Throwable> errorMethodHandler,
                                                           RoutingContext context,
                                                           Map<String, String> headers,
                                                           Encoder encoder,
                                                           Consumer<Throwable> errorHandler,
                                                           ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                           int httpStatusCode, int httpErrorCode,
                                                           int retryCount, long timeout,
                                                           long circuitBreakerTimeout,
                                                           RecursiveExecutor executor,
                                                           RetryExecutor retry,
                                                           AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<T> stringSupplier = createSupplier(stringFunction, event);
        if (circuitBreakerTimeout == DEFAULT_VALUE) {
            statelessExecution(methodId,
                    id,
                    message,
                    stringFunction,
                    deliveryOptions,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount, timeout,
                    circuitBreakerTimeout,
                    executor, retry,
                    event, stringSupplier);
        } else {
            statefulExecution(methodId,
                    id, message,
                    stringFunction,
                    deliveryOptions,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount, timeout,
                    circuitBreakerTimeout,
                    executor, retry,
                    event, stringSupplier);
        }
    }

    private static <T> void statelessExecution(String methodId,
                                               String id, Object message,
                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                               DeliveryOptions options,
                                               Vertx vertx, Throwable t,
                                               Consumer<Throwable> errorMethodHandler,
                                               RoutingContext context, Map<String, String> headers,
                                               Encoder encoder, Consumer<Throwable> errorHandler,
                                               ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                               int httpStatusCode, int httpErrorCode,
                                               int retryCount, long timeout,
                                               long circuitBreakerTimeout, RecursiveExecutor executor,
                                               RetryExecutor retry,
                                               AsyncResult<Message<Object>> event, ThrowableFutureConsumer<T> stringSupplier) {
        if (event.succeeded() || (event.failed() && retryCount <= 0)) {
            executor.execute(methodId,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    stringSupplier,
                    null,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout, circuitBreakerTimeout);
        } else if (event.failed() && retryCount > 0) {
            // retry operation
            final Throwable cause = event.cause();
            retryOperation(methodId,
                    id, message,
                    stringFunction,
                    options, vertx,
                    cause,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout,
                    circuitBreakerTimeout, retry);
        }
    }

    private static <T> void statefulExecution(String methodId,
                                              String id, Object message,
                                              ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                              DeliveryOptions deliveryOptions,
                                              Vertx vertx, Throwable t,
                                              Consumer<Throwable> errorMethodHandler,
                                              RoutingContext context,
                                              Map<String, String> headers,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                              int httpStatusCode, int httpErrorCode,
                                              int retryCount, long timeout,
                                              long circuitBreakerTimeout, RecursiveExecutor executor,
                                              RetryExecutor retry,
                                              AsyncResult<Message<Object>> event,
                                              ThrowableFutureConsumer<T> stringSupplier) {
        if (event.succeeded()) {
            executor.execute(methodId,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    stringSupplier,
                    null,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout, circuitBreakerTimeout);
        } else {
            statefulErrorHandling(methodId,
                    id,
                    message,
                    stringFunction,
                    deliveryOptions,
                    vertx,
                    errorMethodHandler,
                    context,
                    headers,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode, retryCount,
                    timeout, circuitBreakerTimeout,
                    executor, retry, event);
        }
    }


    private static <T> void statefulErrorHandling(String methodId,
                                                  String id, Object message,
                                                  ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                                  DeliveryOptions deliveryOptions,
                                                  Vertx vertx, Consumer<Throwable> errorMethodHandler,
                                                  RoutingContext context, Map<String, String> headers,
                                                  Encoder encoder, Consumer<Throwable> errorHandler,
                                                  ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                  int httpStatusCode, int httpErrorCode,
                                                  int retryCount, long timeout,
                                                  long circuitBreakerTimeout,
                                                  RecursiveExecutor executor,
                                                  RetryExecutor retry,
                                                  AsyncResult<Message<Object>> event) {

        executeLocked((lock, counter) ->
                decrementAndExecute(counter, valHandler -> {
                    if (valHandler.succeeded()) {
                        long count = valHandler.result();
                        if (count <= DEFAULT_VALUE) {
                            openCircuitAndHandleError(methodId,
                                    vertx,
                                    errorMethodHandler,
                                    context, headers,
                                    encoder, errorHandler,
                                    onFailureRespond,
                                    httpStatusCode,
                                    httpErrorCode,
                                    retryCount, timeout,
                                    circuitBreakerTimeout,
                                    executor, event,
                                    lock, counter);
                        } else {
                            lock.release();
                            retryOperation(methodId,
                                    id,
                                    message,
                                    stringFunction,
                                    deliveryOptions,
                                    vertx,
                                    event.cause(),
                                    errorMethodHandler,
                                    context, headers,
                                    encoder, errorHandler,
                                    onFailureRespond,
                                    httpStatusCode, httpErrorCode,
                                    retryCount, timeout,
                                    circuitBreakerTimeout, retry);
                        }
                    } else {
                        final Throwable cause = valHandler.cause();
                        handleError(methodId,
                                vertx,
                                errorMethodHandler,
                                context, headers,
                                encoder,
                                errorHandler,
                                onFailureRespond,
                                httpStatusCode,
                                httpErrorCode,
                                retryCount, timeout,
                                circuitBreakerTimeout,
                                executor, lock, cause);
                    }
                }), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout, executor);


    }

    private static void decrementAndExecute(Counter counter, Handler<AsyncResult<Long>> asyncResultHandler) {
        counter.decrementAndGet(asyncResultHandler);
    }


    private static <T> void executeLocked(LockedConsumer consumer,
                                          String _methodId,
                                          Vertx vertx,
                                          Consumer<Throwable> errorHandler,
                                          ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                          Consumer<Throwable> errorMethodHandler,
                                          RoutingContext context,
                                          Map<String, String> headers,
                                          Encoder encoder,
                                          int httpStatusCode, int httpErrorCode,
                                          int retryCount, long timeout,
                                          long circuitBreakerTimeout, RecursiveExecutor executor) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(_methodId, DEFAULT_LOCK_TIMEOUT, lockHandler -> {
            if (lockHandler.succeeded()) {
                final Lock lock = lockHandler.result();
                sharedData.getCounter(_methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        consumer.execute(lock, resultHandler.result());
                    } else {
                        final Throwable cause = resultHandler.cause();
                        handleError(_methodId,
                                vertx,
                                errorMethodHandler,
                                context,
                                headers,
                                encoder,
                                errorHandler,
                                onFailureRespond,
                                httpStatusCode,
                                httpErrorCode,
                                retryCount,
                                timeout,
                                circuitBreakerTimeout,
                                executor, lock, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(_methodId,
                        vertx,
                        errorMethodHandler,
                        context, headers,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout,
                        executor, null, cause);
            }

        });
    }

    private static <T> void openCircuitAndHandleError(String methodId,
                                                      Vertx vertx,
                                                      Consumer<Throwable> errorMethodHandler,
                                                      RoutingContext context, Map<String, String> headers,
                                                      Encoder encoder, Consumer<Throwable> errorHandler,
                                                      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                      int httpStatusCode, int httpErrorCode,
                                                      int retryCount, long timeout,
                                                      long circuitBreakerTimeout, RecursiveExecutor executor,
                                                      AsyncResult<Message<Object>> event, Lock lock, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
        counter.addAndGet(LOCK_VALUE, val -> {
            final Throwable cause = event.cause();
            handleError(methodId,
                    vertx,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode, retryCount,
                    timeout, circuitBreakerTimeout,
                    executor, lock, cause);

        });
    }

    private static <T> void handleError(String methodId,
                                        Vertx vertx,
                                        Consumer<Throwable> errorMethodHandler,
                                        RoutingContext context, Map<String, String> headers,
                                        Encoder encoder,
                                        Consumer<Throwable> errorHandler,
                                        ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                        int httpStatusCode, int httpErrorCode,
                                        int retryCount, long timeout,
                                        long circuitBreakerTimeout, RecursiveExecutor executor,
                                        Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        final ThrowableFutureConsumer<T> failConsumer = (future) -> future.fail(cause);
        executor.execute(methodId,
                vertx, cause,
                errorMethodHandler,
                context, headers,
                failConsumer,
                null,
                encoder, errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout, circuitBreakerTimeout);

    }

    private static <T> void retryOperation(String methodId,
                                           String id,
                                           Object message,
                                           ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function,
                                           DeliveryOptions deliveryOptions,
                                           Vertx vertx, Throwable t,
                                           Consumer<Throwable> errorMethodHandler,
                                           RoutingContext context,
                                           Map<String, String> headers,
                                           Encoder encoder,
                                           Consumer<Throwable> errorHandler,
                                           ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                           int httpStatusCode, int httpErrorCode,
                                           int retryCount, long timeout,
                                           long circuitBreakerTimeout, RetryExecutor retry) {
        ResponseExecution.handleError(errorHandler, t);
        retry.execute(methodId,
                id, message,
                function,
                deliveryOptions,
                vertx, t,
                errorMethodHandler,
                context, headers,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout, circuitBreakerTimeout);
    }

    private static <T> ThrowableFutureConsumer<T> createSupplier(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function, AsyncResult<Message<Object>> event) {
        return (future) -> {
            if (event.failed()) {
                future.fail(event.cause());
            } else {
                function.accept(event, future);
            }
        };
    }


    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
    }


}
