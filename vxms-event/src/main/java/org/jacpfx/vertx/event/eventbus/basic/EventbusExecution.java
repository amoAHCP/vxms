package org.jacpfx.vertx.event.eventbus.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vertx.event.response.basic.ResponseExecution;
import org.jacpfx.vertx.event.interfaces.basic.RetryExecutor;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Handles event-bus call and non-blocking execution of the message to create an event-bus response
 */
public class EventbusExecution {

    public static final long LOCK_VALUE = -1l;
    public static final int DEFAULT_LOCK_TIMEOUT = 2000;
    public static final long NO_TIMEOUT = 0l;


    /**
     * Send event-bus message and process the result in the passed function for the execution chain
     *
     * @param methodId                the method identifier
     * @param targetId                the event-bus target id
     * @param message                 the message to send
     * @param function                the function to process the result message
     * @param requestDeliveryOptions the event-bus delivery options
     * @param vertx  the vertx instance
     * @param errorMethodHandler the error-method handler
     * @param requestMessage the request message to respond after chain execution
     * @param encoder the encoder to serialize the response object
     * @param errorHandler the error handler
     * @param onFailureRespond the function that takes a Future with the alternate response value in case of failure
     * @param responseDeliveryOptions the delivery options for the event response
     * @param retryCount the amount of retries before failure execution is triggered
     * @param timeout the amount of time before the execution will be aborted
     * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
     * @param executor the typed executor to process the chain
     * @param retryExecutor the typed retry executor of the chain
     * @param <T> the type of response
     */
    public static <T> void sendMessageAndSupplyHandler(String methodId,
                                                       String targetId,
                                                       Object message,
                                                       ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function,
                                                       DeliveryOptions requestDeliveryOptions,
                                                       Vertx vertx,
                                                       Consumer<Throwable> errorMethodHandler,
                                                       Message<Object> requestMessage,
                                                       Encoder encoder,
                                                       Consumer<Throwable> errorHandler,
                                                       ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                       DeliveryOptions responseDeliveryOptions,
                                                       int retryCount,
                                                       long timeout,
                                                       long circuitBreakerTimeout,
                                                       RecursiveExecutor executor,
                                                       RetryExecutor retryExecutor) {

        if (circuitBreakerTimeout == 0l) {
            executeDefaultState(targetId,
                    message,
                    function,
                    requestDeliveryOptions,
                    methodId, vertx,
                    errorMethodHandler,
                    requestMessage,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount, timeout, circuitBreakerTimeout, executor, retryExecutor, null);

        } else {
            executeStateful(targetId,
                    message,
                    function,
                    requestDeliveryOptions,
                    methodId, vertx,
                    errorMethodHandler,
                    requestMessage,
                    encoder, errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount,
                    timeout,
                    circuitBreakerTimeout, executor, retryExecutor);
        }

    }

    private static <T> void executeStateful(String targetId,
                                            Object message,
                                            ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                            DeliveryOptions requestDeliveryOptions,
                                            String methodId,
                                            Vertx vertx,
                                            Consumer<Throwable> errorMethodHandler,
                                            Message<Object> requestMessage,
                                            Encoder encoder,
                                            Consumer<Throwable> errorHandler,
                                            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                            DeliveryOptions responseDeliveryOptions,
                                            int retryCount, long timeout, long circuitBreakerTimeout,
                                            RecursiveExecutor executor, RetryExecutor retry) {

        executeLocked(((lock, counter) ->
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == 0) {
                        executeInitialState(targetId,
                                message,
                                objectFunction,
                                requestDeliveryOptions,
                                methodId, vertx,
                                errorMethodHandler,
                                requestMessage,
                                encoder, errorHandler,
                                onFailureRespond,
                                responseDeliveryOptions,
                                retryCount, timeout,
                                circuitBreakerTimeout, executor, retry,
                                lock, counter);
                    } else if (currentVal > 0) {
                        executeDefaultState(targetId,
                                message,
                                objectFunction,
                                requestDeliveryOptions,
                                methodId, vertx,
                                errorMethodHandler,
                                requestMessage,
                                encoder, errorHandler,
                                onFailureRespond,
                                responseDeliveryOptions,
                                retryCount, timeout,
                                circuitBreakerTimeout, executor, retry, lock);
                    } else {
                        executeErrorState(methodId,
                                vertx,
                                errorMethodHandler,
                                requestMessage,
                                encoder, errorHandler,
                                onFailureRespond,
                                responseDeliveryOptions,
                                retryCount, timeout,
                                circuitBreakerTimeout, executor, lock);
                    }
                })), methodId, vertx, errorMethodHandler, requestMessage, encoder, errorHandler, onFailureRespond, responseDeliveryOptions, retryCount, timeout, circuitBreakerTimeout, executor);
    }


    private static <T> void executeInitialState(String targetId,
                                                Object message,
                                                ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                                DeliveryOptions requestDeliveryOptions,
                                                String methodId,
                                                Vertx vertx,
                                                Consumer<Throwable> errorMethodHandler,
                                                Message<Object> requestMessage,
                                                Encoder encoder,
                                                Consumer<Throwable> errorHandler,
                                                ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                DeliveryOptions responseDeliveryOptions,
                                                int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor, RetryExecutor retry, Lock lock, Counter counter) {
        int incrementCounter = retryCount + 1;
        counter.addAndGet(Integer.valueOf(incrementCounter).longValue(), rHandler -> executeDefaultState(targetId,
                message,
                objectFunction,
                requestDeliveryOptions,
                methodId, vertx,
                errorMethodHandler,
                requestMessage,
                encoder,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount, timeout, circuitBreakerTimeout, executor, retry, lock));
    }

    private static <T> void executeDefaultState(String targetId,
                                                Object message,
                                                ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                                DeliveryOptions requestDeliveryOptions,
                                                String methodId,
                                                Vertx vertx,
                                                Consumer<Throwable> errorMethodHandler,
                                                Message<Object> requestMessage,
                                                Encoder encoder,
                                                Consumer<Throwable> errorHandler,
                                                ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                DeliveryOptions responseDeliveryOptions,
                                                int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor, RetryExecutor retry, Lock lock) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        vertx.
                eventBus().
                send(targetId, message, requestDeliveryOptions,
                        event ->
                                createSupplierAndExecute(targetId,
                                        message,
                                        objectFunction,
                                        requestDeliveryOptions,
                                        methodId, vertx,
                                        errorMethodHandler,
                                        requestMessage,
                                        encoder, errorHandler,
                                        onFailureRespond,
                                        responseDeliveryOptions,
                                        retryCount, timeout,
                                        circuitBreakerTimeout, executor, retry, event));
    }

    private static <T> void executeErrorState(String methodId,
                                              Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler,
                                              Message<Object> requestMessage,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                              DeliveryOptions responseDeliveryOptions,
                                              int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor,
                                              Lock lock) {
        final Throwable cause = Future.failedFuture("circuit open").cause();
        handleError(methodId,
                vertx, errorMethodHandler,
                requestMessage, encoder,
                errorHandler, onFailureRespond,
                responseDeliveryOptions,
                retryCount, timeout,
                circuitBreakerTimeout, executor, lock, cause);
    }

    private static <T> void createSupplierAndExecute(String targetId,
                                                     Object message,
                                                     ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                                     DeliveryOptions requestDeliveryOptions,
                                                     String methodId,
                                                     Vertx vertx,
                                                     Consumer<Throwable> errorMethodHandler,
                                                     Message<Object> requestMessage,
                                                     Encoder encoder,
                                                     Consumer<Throwable> errorHandler,
                                                     ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                     DeliveryOptions responseDeliveryOptions,
                                                     int retryCount, long timeout, long circuitBreakerTimeout,
                                                     RecursiveExecutor executor, RetryExecutor retry, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<T> objectConsumer = createSupplier(objectFunction, event);
        if (circuitBreakerTimeout == NO_TIMEOUT) {
            statelessExecution(targetId,
                    message,
                    objectFunction,
                    requestDeliveryOptions,
                    methodId, vertx,
                    errorMethodHandler,
                    requestMessage,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount, timeout, circuitBreakerTimeout, executor, retry, event, objectConsumer);
        } else {
            statefulExecution(targetId,
                    message,
                    objectFunction,
                    requestDeliveryOptions,
                    methodId, vertx,
                    errorMethodHandler,
                    requestMessage,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount, timeout, circuitBreakerTimeout, executor, retry, event, objectConsumer);

        }


    }

    private static <T> void statelessExecution(String targetId,
                                               Object message,
                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                               DeliveryOptions requestDeliveryOptions,
                                               String methodId,
                                               Vertx vertx,
                                               Consumer<Throwable> errorMethodHandler,
                                               Message<Object> requestMessage,
                                               Encoder encoder,
                                               Consumer<Throwable> errorHandler,
                                               ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                               DeliveryOptions responseDeliveryOptions,
                                               int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor,
                                               RetryExecutor retry,
                                               AsyncResult<Message<Object>> event,
                                               ThrowableFutureConsumer<T> objectConsumer) {
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            executor.execute(methodId,
                    vertx, event.cause(),
                    errorMethodHandler,
                    requestMessage,
                    objectConsumer,
                    encoder, errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount, timeout, circuitBreakerTimeout);
        } else if (event.failed() && retryCount > 0) {
            retryFunction(targetId,
                    message,
                    objectFunction,
                    requestDeliveryOptions,
                    methodId, vertx,
                    event.cause(), errorMethodHandler,
                    requestMessage, encoder,
                    errorHandler, onFailureRespond,
                    responseDeliveryOptions, retryCount,
                    timeout, circuitBreakerTimeout, retry);
        }
    }

    private static <T> void statefulExecution(String targetId,
                                              Object message,
                                              ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                              DeliveryOptions requestDeliveryOptions,
                                              String methodId,
                                              Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler,
                                              Message<Object> requestMessage,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                              DeliveryOptions responseDeliveryOptions,
                                              int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor,
                                              RetryExecutor retry, AsyncResult<Message<Object>> event, ThrowableFutureConsumer<T> objectConsumer) {
        if (event.succeeded()) {
            executor.execute(methodId, vertx, event.cause(), errorMethodHandler, requestMessage, objectConsumer,
                    encoder, errorHandler, onFailureRespond, responseDeliveryOptions, retryCount, timeout, circuitBreakerTimeout);
        } else {
            statefulErrorHandling(targetId,
                    message, objectFunction,
                    requestDeliveryOptions,
                    methodId, vertx,
                    event.cause(),
                    errorMethodHandler,
                    requestMessage,
                    encoder, errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount, timeout,
                    circuitBreakerTimeout,
                    executor, retry, event);
        }
    }

    private static <T> void statefulErrorHandling(String targetId,
                                                  Object message,
                                                  ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                                  DeliveryOptions requestDeliveryOptions,
                                                  String methodId,
                                                  Vertx vertx,
                                                  Throwable t,
                                                  Consumer<Throwable> errorMethodHandler,
                                                  Message<Object> requestMessage,
                                                  Encoder encoder,
                                                  Consumer<Throwable> errorHandler,
                                                  ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                  DeliveryOptions responseDeliveryOptions,
                                                  int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor, RetryExecutor retry, AsyncResult<Message<Object>> event) {

        executeLocked((lock, counter) ->
                        decrementAndExecute(counter,
                                valHandler -> {
                                    if (valHandler.succeeded()) {
                                        long count = valHandler.result();
                                        if (count <= 0) {
                                            openCircuitAndHandleError(methodId,
                                                    vertx,
                                                    errorMethodHandler,
                                                    requestMessage,
                                                    encoder,
                                                    errorHandler,
                                                    onFailureRespond,
                                                    responseDeliveryOptions,
                                                    retryCount, timeout,
                                                    circuitBreakerTimeout,
                                                    executor, event,
                                                    lock, counter);
                                        } else {
                                            lock.release();
                                            retryFunction(targetId,
                                                    message,
                                                    objectFunction,
                                                    requestDeliveryOptions,
                                                    methodId,
                                                    vertx, t,
                                                    errorMethodHandler,
                                                    requestMessage,
                                                    encoder,
                                                    errorHandler,
                                                    onFailureRespond,
                                                    responseDeliveryOptions,
                                                    retryCount, timeout,
                                                    circuitBreakerTimeout, retry);
                                        }
                                    } else {
                                        final Throwable cause = valHandler.cause();
                                        handleError(methodId, vertx, errorMethodHandler, requestMessage,
                                                encoder, errorHandler, onFailureRespond, responseDeliveryOptions, retryCount, timeout, circuitBreakerTimeout, executor, lock, cause);
                                    }
                                }), methodId, vertx, errorMethodHandler, requestMessage,
                encoder, errorHandler, onFailureRespond, responseDeliveryOptions, retryCount, timeout, circuitBreakerTimeout, executor);


    }

    private static void decrementAndExecute(Counter counter, Handler<AsyncResult<Long>> asyncResultHandler) {
        counter.decrementAndGet(asyncResultHandler);
    }


    private static <T> void executeLocked(LockedConsumer consumer,
                                          String methodId,
                                          Vertx vertx,
                                          Consumer<Throwable> errorMethodHandler,
                                          Message<Object> requestMessage,
                                          Encoder encoder,
                                          Consumer<Throwable> errorHandler,
                                          ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                          DeliveryOptions responseDeliveryOptions,
                                          int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(methodId, DEFAULT_LOCK_TIMEOUT, lockHandler -> {
            if (lockHandler.succeeded()) {
                final Lock lock = lockHandler.result();
                sharedData.getCounter(methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        consumer.execute(lock, resultHandler.result());
                    } else {
                        final Throwable cause = resultHandler.cause();
                        handleError(methodId,
                                vertx,
                                errorMethodHandler,
                                requestMessage,
                                encoder, errorHandler,
                                onFailureRespond,
                                responseDeliveryOptions,
                                retryCount, timeout,
                                circuitBreakerTimeout,
                                executor, lock, cause);
                    }
                });
            } else {
                final Throwable cause = lockHandler.cause();
                handleError(methodId,
                        vertx,
                        errorMethodHandler,
                        requestMessage,
                        encoder, errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount, timeout,
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
                                                      Message<Object> requestMessage,
                                                      Encoder encoder,
                                                      Consumer<Throwable> errorHandler,
                                                      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                                      DeliveryOptions responseDeliveryOptions,
                                                      int retryCount, long timeout, long circuitBreakerTimeout,
                                                      RecursiveExecutor executor, AsyncResult<Message<Object>> event,
                                                      Lock lock, Counter counter) {
        resetLockTimer(vertx, retryCount, circuitBreakerTimeout, counter);
        lockAndHandle(counter, val -> {
            final Throwable cause = event.cause();
            handleError(methodId,
                    vertx,
                    errorMethodHandler,
                    requestMessage,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount,
                    timeout,
                    circuitBreakerTimeout,
                    executor, lock, cause);
        });
    }

    private static void lockAndHandle(Counter counter, Handler<AsyncResult<Long>> asyncResultHandler) {
        counter.addAndGet(LOCK_VALUE, asyncResultHandler);
    }

    private static void resetLockTimer(Vertx vertx, int retryCount, long circuitBreakerTimeout, Counter counter) {
        vertx.setTimer(circuitBreakerTimeout, timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {
        }));
    }

    private static <T> void handleError(String methodId,
                                        Vertx vertx,
                                        Consumer<Throwable> errorMethodHandler,
                                        Message<Object> requestMessage,
                                        Encoder encoder,
                                        Consumer<Throwable> errorHandler,
                                        ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                        DeliveryOptions responseDeliveryOptions,
                                        int retryCount, long timeout, long circuitBreakerTimeout, RecursiveExecutor executor, Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        final ThrowableFutureConsumer<T> failConsumer = (future) -> future.fail(cause);
        executor.execute(methodId,
                vertx,
                cause,
                errorMethodHandler,
                requestMessage,
                failConsumer,
                encoder,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount,
                timeout, circuitBreakerTimeout);
    }

    private static <T> void retryFunction(String targetId,
                                          Object message,
                                          ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
                                          DeliveryOptions requestDeliveryOptions,
                                          String methodId,
                                          Vertx vertx,
                                          Throwable t,
                                          Consumer<Throwable> errorMethodHandler,
                                          Message<Object> requestMessage,
                                          Encoder encoder,
                                          Consumer<Throwable> errorHandler,
                                          ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                          DeliveryOptions responseDeliveryOptions,
                                          int retryCount, long timeout, long circuitBreakerTimeout, RetryExecutor retry) {
        ResponseExecution.handleError(errorHandler, t);
        retry.execute(targetId,
                message,
                objectFunction,
                requestDeliveryOptions,
                methodId, vertx, t,
                errorMethodHandler,
                requestMessage,
                null,
                encoder,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    private static <T> ThrowableFutureConsumer<T> createSupplier(ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction, AsyncResult<Message<Object>> event) {
        return (future) -> {

            if (event.failed()) {
                future.fail(event.cause());
            } else {
                objectFunction.accept(event, future);
            }

        };
    }
}
