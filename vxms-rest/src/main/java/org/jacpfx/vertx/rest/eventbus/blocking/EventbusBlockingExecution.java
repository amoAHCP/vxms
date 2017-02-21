package org.jacpfx.vertx.rest.eventbus.blocking;

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
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.RecursiveBlockingExecutor;
import org.jacpfx.vertx.rest.interfaces.blocking.RetryBlockingExecutor;
import org.jacpfx.vertx.rest.response.basic.ResponseExecution;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Handles event-bus call and blocking execution of the message to create a rest response
 */
public class EventbusBlockingExecution {


    public static final long LOCK_VALUE = -1l;
    public static final int DEFAULT_LOCK_TIMEOUT = 2000;
    public static final long NO_TIMEOUT = 0l;

    /**
     * Send event-bus message and process the result in the passed function for blocking execution chain
     *
     * @param methodId              the method identifier
     * @param targetId              the event-bus target id
     * @param message               the message to send
     * @param function              the function to process the result message
     * @param deliveryOptions       the event-bus delivery options
     * @param vertx                 the vertx instance
     * @param failure               the failure thrown while task execution
     * @param errorMethodHandler    the error handler
     * @param context               the vertx routing context
     * @param headers               the headers to pass to the response
     * @param encoder               the encoder to encode your objects
     * @param errorHandler          the error handler
     * @param onFailureRespond      the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode        the http status code to set for response
     * @param httpErrorCode         the http error code to set in case of failure handling
     * @param retryCount            the amount of retries before failure execution is triggered
     * @param timeout               the amount of time before the execution will be aborted
     * @param delay                 the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
     * @param executor              the typed executor to process the chain
     * @param retryExecutor         the typed retry executor of the chain
     * @param <T>                   the type of response
     */
    public static <T> void sendMessageAndSupplyHandler(String methodId,
                                                       String targetId,
                                                       Object message,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                                       DeliveryOptions deliveryOptions,
                                                       Vertx vertx,
                                                       Throwable failure,
                                                       Consumer<Throwable> errorMethodHandler,
                                                       RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder,
                                                       Consumer<Throwable> errorHandler,
                                                       ThrowableFunction<Throwable, T> onFailureRespond,
                                                       int httpStatusCode,
                                                       int httpErrorCode,
                                                       int retryCount,
                                                       long timeout,
                                                       long delay,
                                                       long circuitBreakerTimeout,
                                                       RecursiveBlockingExecutor executor,
                                                       RetryBlockingExecutor retryExecutor) {
        if (circuitBreakerTimeout == 0l) {
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
                    delay, circuitBreakerTimeout,
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
                    delay, circuitBreakerTimeout,
                    executor, retryExecutor);
        }
    }

    private static <T> void executeStateful(String methodId,
                                            String id, Object message,
                                            ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                            DeliveryOptions deliveryOptions,
                                            Vertx vertx, Throwable t,
                                            Consumer<Throwable> errorMethodHandler,
                                            RoutingContext context, Map<String, String> headers,
                                            Encoder encoder,
                                            Consumer<Throwable> errorHandler,
                                            ThrowableFunction<Throwable, T> onFailureRespond,
                                            int httpStatusCode, int httpErrorCode,
                                            int retryCount, long timeout, long delay,
                                            long circuitBreakerTimeout,
                                            RecursiveBlockingExecutor executor,
                                            RetryBlockingExecutor retry) {

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
                                delay,
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
                                delay, circuitBreakerTimeout,
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
                                delay, circuitBreakerTimeout,
                                executor, lock);
                    }
                })), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, executor);
    }

    private static <T> void executeInitialState(String methodId,
                                                String id, Object message,
                                                ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                                DeliveryOptions deliveryOptions,
                                                Vertx vertx, Throwable t,
                                                Consumer<Throwable> errorMethodHandler,
                                                RoutingContext context, Map<String, String> headers,
                                                Encoder encoder,
                                                Consumer<Throwable> errorHandler,
                                                ThrowableFunction<Throwable, T> onFailureRespond,
                                                int httpStatusCode, int httpErrorCode,
                                                int retryCount, long timeout, long delay,
                                                long circuitBreakerTimeout,
                                                RecursiveBlockingExecutor executor,
                                                RetryBlockingExecutor retry,
                                                Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), rHandler ->
                executeDefaultState(methodId,
                        id, message,
                        function,
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
                        delay,
                        circuitBreakerTimeout,
                        executor, retry, lock));
    }

    private static <T> void executeDefaultState(String methodId,
                                                String id, Object message,
                                                ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                                DeliveryOptions deliveryOptions,
                                                Vertx vertx, Throwable t,
                                                Consumer<Throwable> errorMethodHandler,
                                                RoutingContext context, Map<String, String> headers,
                                                Encoder encoder,
                                                Consumer<Throwable> errorHandler,
                                                ThrowableFunction<Throwable, T> onFailureRespond,
                                                int httpStatusCode, int httpErrorCode,
                                                int retryCount, long timeout, long delay,
                                                long circuitBreakerTimeout,
                                                RecursiveBlockingExecutor executor,
                                                RetryBlockingExecutor retry,
                                                Lock lock) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(methodId,
                                        id, message,
                                        function,
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
                                        delay, circuitBreakerTimeout,
                                        executor, retry, event));
    }

    private static <T> void executeErrorState(String methodId,
                                              Vertx vertx,
                                              Consumer<Throwable> errorMethodHandler,
                                              RoutingContext context, Map<String, String> headers,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableFunction<Throwable, T> onFailureRespond,
                                              int httpStatusCode, int httpErrorCode,
                                              int retryCount, long timeout, long delay,
                                              long circuitBreakerTimeout,
                                              RecursiveBlockingExecutor executor,
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
                delay,
                circuitBreakerTimeout,
                executor,
                lock, cause);
    }


    private static <T> void createStringSupplierAndExecute(String methodId,
                                                           String targetId, Object message,
                                                           ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                                           DeliveryOptions deliveryOptions,
                                                           Vertx vertx, Throwable t,
                                                           Consumer<Throwable> errorMethodHandler,
                                                           RoutingContext context, Map<String, String> headers,
                                                           Encoder encoder,
                                                           Consumer<Throwable> errorHandler,
                                                           ThrowableFunction<Throwable, T> onFailureRespond,
                                                           int httpStatusCode, int httpErrorCode,
                                                           int retryCount, long timeout, long delay,
                                                           long circuitBreakerTimeout,
                                                           RecursiveBlockingExecutor executor,
                                                           RetryBlockingExecutor retry,
                                                           AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<T> supplier = createSupplier(methodId,
                targetId,
                message,
                function,
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
                delay,
                circuitBreakerTimeout,
                retry, event);
        if (circuitBreakerTimeout == NO_TIMEOUT) {
            statelessExecution(methodId,
                    targetId,
                    message,
                    function,
                    deliveryOptions,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount, timeout,
                    delay,
                    circuitBreakerTimeout,
                    executor, retry,
                    event, supplier);
        } else {
            statefulExecution(methodId,
                    targetId, message,
                    function,
                    deliveryOptions,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount, timeout,
                    delay,
                    circuitBreakerTimeout,
                    executor, retry,
                    event, supplier);
        }
    }

    private static <T> void statelessExecution(String methodId,
                                               String id, Object message,
                                               ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                               DeliveryOptions deliveryOptions,
                                               Vertx vertx, Throwable t,
                                               Consumer<Throwable> errorMethodHandler,
                                               RoutingContext context, Map<String, String> headers,
                                               Encoder encoder,
                                               Consumer<Throwable> errorHandler,
                                               ThrowableFunction<Throwable, T> onFailureRespond,
                                               int httpStatusCode, int httpErrorCode,
                                               int retryCount, long timeout, long delay,
                                               long circuitBreakerTimeout,
                                               RecursiveBlockingExecutor executor,
                                               RetryBlockingExecutor retry,
                                               AsyncResult<Message<Object>> event, ThrowableSupplier<T> supplier) {
        if (event.succeeded() || (event.failed() && retryCount <= 0)) {
            executor.execute(methodId,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    supplier,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout, delay, circuitBreakerTimeout);
        } else if (event.failed() && retryCount > 0) {
            // retry operation
            final Throwable cause = event.cause();
            retryOperation(methodId,
                    id, message,
                    function,
                    deliveryOptions, vertx,
                    cause,
                    errorMethodHandler,
                    context, headers,
                    encoder, errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout,
                    delay,
                    circuitBreakerTimeout, retry);
        }
    }

    private static <T> void statefulExecution(String methodId,
                                              String id, Object message,
                                              ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                              DeliveryOptions deliveryOptions,
                                              Vertx vertx, Throwable t,
                                              Consumer<Throwable> errorMethodHandler,
                                              RoutingContext context, Map<String, String> headers,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableFunction<Throwable, T> onFailureRespond,
                                              int httpStatusCode, int httpErrorCode,
                                              int retryCount, long timeout, long delay,
                                              long circuitBreakerTimeout, RecursiveBlockingExecutor executor,
                                              RetryBlockingExecutor retry,
                                              AsyncResult<Message<Object>> event,
                                              ThrowableSupplier<T> supplier) {
        if (event.succeeded()) {
            executor.execute(methodId,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    supplier,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout, delay, circuitBreakerTimeout);
        } else {
            statefulErrorHandling(methodId,
                    id,
                    message,
                    function,
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
                    timeout, delay,
                    circuitBreakerTimeout,
                    executor, retry, event);
        }
    }


    private static <T> void statefulErrorHandling(String methodId,
                                                  String id,
                                                  Object message,
                                                  ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                                  DeliveryOptions deliveryOptions,
                                                  Vertx vertx,
                                                  Consumer<Throwable> errorMethodHandler,
                                                  RoutingContext context, Map<String, String> headers,
                                                  Encoder encoder,
                                                  Consumer<Throwable> errorHandler,
                                                  ThrowableFunction<Throwable, T> onFailureRespond,
                                                  int httpStatusCode, int httpErrorCode,
                                                  int retryCount, long timeout, long delay,
                                                  long circuitBreakerTimeout,
                                                  RecursiveBlockingExecutor executor,
                                                  RetryBlockingExecutor retry,
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
                                    delay,
                                    circuitBreakerTimeout,
                                    executor, event,
                                    lock, counter);
                        } else {
                            lock.release();
                            retryOperation(methodId,
                                    id,
                                    message,
                                    function,
                                    deliveryOptions,
                                    vertx,
                                    event.cause(),
                                    errorMethodHandler,
                                    context, headers,
                                    encoder, errorHandler,
                                    onFailureRespond,
                                    httpStatusCode, httpErrorCode,
                                    retryCount, timeout,
                                    delay,
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
                                delay,
                                executor, lock, cause);
                    }
                }), methodId, vertx, errorHandler, onFailureRespond, errorMethodHandler, context, headers, encoder, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout, executor);


    }

    private static void decrementAndExecute(Counter counter, Handler<AsyncResult<Long>> asyncResultHandler) {
        counter.decrementAndGet(asyncResultHandler);
    }


    private static <T> void executeLocked(LockedConsumer consumer,
                                          String _methodId,
                                          Vertx vertx,
                                          Consumer<Throwable> errorHandler,
                                          ThrowableFunction<Throwable, T> onFailureRespond,
                                          Consumer<Throwable> errorMethodHandler,
                                          RoutingContext context,
                                          Map<String, String> headers,
                                          Encoder encoder,
                                          int httpStatusCode, int httpErrorCode,
                                          int retryCount, long timeout,
                                          long delay,
                                          long circuitBreakerTimeout, RecursiveBlockingExecutor executor) {
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
                                delay,
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
                        delay,
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
                                                      ThrowableFunction<Throwable, T> onFailureRespond,
                                                      int httpStatusCode, int httpErrorCode,
                                                      int retryCount, long timeout,
                                                      long delay,
                                                      long circuitBreakerTimeout, RecursiveBlockingExecutor executor,
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
                    timeout, delay, circuitBreakerTimeout,
                    executor, lock, cause);

        });
    }

    private static <T> void handleError(String methodId,
                                        Vertx vertx,
                                        Consumer<Throwable> errorMethodHandler,
                                        RoutingContext context, Map<String, String> headers,
                                        Encoder encoder,
                                        Consumer<Throwable> errorHandler,
                                        ThrowableFunction<Throwable, T> onFailureRespond,
                                        int httpStatusCode, int httpErrorCode,
                                        int retryCount, long timeout,
                                        long delay,
                                        long circuitBreakerTimeout, RecursiveBlockingExecutor executor,
                                        Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        ThrowableSupplier<byte[]> failConsumer = () -> {
            assert cause != null;
            throw cause;
        };
        executor.execute(methodId,
                vertx, cause,
                errorMethodHandler,
                context, headers,
                failConsumer,
                encoder, errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout, delay, circuitBreakerTimeout);

    }

    private static <T> void retryOperation(String methodId,
                                           String id,
                                           Object message,
                                           ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                           DeliveryOptions deliveryOptions,
                                           Vertx vertx, Throwable t,
                                           Consumer<Throwable> errorMethodHandler,
                                           RoutingContext context,
                                           Map<String, String> headers,
                                           Encoder encoder,
                                           Consumer<Throwable> errorHandler,
                                           ThrowableFunction<Throwable, T> onFailureRespond,
                                           int httpStatusCode, int httpErrorCode,
                                           int retryCount, long timeout,
                                           long delay,
                                           long circuitBreakerTimeout, RetryBlockingExecutor retry) {
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
                timeout, delay, circuitBreakerTimeout);
    }

    private static <T> ThrowableSupplier<T> createSupplier(String methodId,
                                                           String targetId,
                                                           Object message,
                                                           ThrowableFunction<AsyncResult<Message<Object>>, T> function,
                                                           DeliveryOptions deliveryOptions,
                                                           Vertx vertx, Throwable t,
                                                           Consumer<Throwable> errorMethodHandler,
                                                           RoutingContext context,
                                                           Map<String, String> headers,
                                                           Encoder encoder,
                                                           Consumer<Throwable> errorHandler,
                                                           ThrowableFunction<Throwable, T> onFailureRespond,
                                                           int httpStatusCode, int httpErrorCode,
                                                           int retryCount, long timeout,
                                                           long delay,
                                                           long circuitBreakerTimeout,
                                                           RetryBlockingExecutor retry,
                                                           AsyncResult<Message<Object>> event) {
        return () -> {
            T resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryOperation(methodId,
                            targetId,
                            message,
                            function,
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
                            delay,
                            circuitBreakerTimeout, retry);
                } else {
                    throw event.cause();
                }
            } else {
                resp = function.apply(event);
            }

            return resp;
        };
    }


    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
    }


}
