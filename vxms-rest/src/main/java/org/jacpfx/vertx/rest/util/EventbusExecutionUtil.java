package org.jacpfx.vertx.rest.util;

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

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusExecutionUtil {


    public static final long LOCK_VALUE = -1l;
    public static final int DEFAULT_LOCK_TIMEOUT = 2000;
    public static final long NO_TIMEOUT = 0l;

    public static <T> void sendMessageAndSupplyStringHandler(String methodId,
                                                             String id, Object message,
                                                             ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> stringFunction,
                                                             DeliveryOptions deliveryOptions,
                                                             Vertx vertx, Throwable t,
                                                             Consumer<Throwable> errorMethodHandler,
                                                             RoutingContext context, Map<String, String> headers,
                                                             Encoder encoder, Consumer<Throwable> errorHandler,
                                                             ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                             int httpStatusCode, int httpErrorCode,
                                                             int retryCount, long timeout,
                                                             long circuitBreakerTimeout,
                                                             RecursiveExecutor executor, RetryExecutor retry) {
        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(methodId,
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
                    executor, retry, null);
        } else {
            executeStateful(methodId,
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
                    retryCount,
                    timeout,
                    circuitBreakerTimeout,
                    executor, retry);
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
                    if (currentVal == 0) {
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
        if (circuitBreakerTimeout == NO_TIMEOUT) {
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
                        if (count <= 0) {
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

    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
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
        ResponseUtil.handleError(errorHandler, t);
        retry.execute(methodId,
                id, message,
                function,
                deliveryOptions,
                vertx, t,
                errorMethodHandler,
                context, headers,
                null, encoder,
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


}
