package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import org.jacpfx.common.ThrowableSupplier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 19.01.16.
 */
public class ResponseAsyncUtil {

    public static <T> void executeRetryAndCatchAsync(String _methodId, ThrowableSupplier<T> _supplier, Future<T> _blockingHandler, Consumer<Throwable> _errorHandler,
                                                     Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler, Vertx vertx,
                                                     int _retry, long _timeout, long _circuitBreakerTimeout, long _delay) {
        if (_circuitBreakerTimeout > 0) {
            executeLocked((lock, counter) -> {
                counter.get(counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == 0) {
                        executeInitialState(_methodId, _supplier, _blockingHandler, _errorHandler, _onFailureRespond,
                                _errorMethodHandler, vertx, _retry, _timeout, _circuitBreakerTimeout, _delay, lock, counter);
                    } else if (currentVal > 0) {
                        executeDefault(_methodId, _supplier, _blockingHandler, _errorHandler, _onFailureRespond,
                                _errorMethodHandler, vertx, _retry, _timeout, _circuitBreakerTimeout, _delay, lock);
                    } else {
                        // TODO add throwable in method signature to pass execption from eventbus execution
                        executeErrorState(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, lock);
                    }
                });
            }, _methodId, vertx, _blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, null);
        } else {
            executeStateless(_supplier, _blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, vertx, _retry, _timeout, _delay);
        }
    }

    public static <T> void executeErrorState(Future<T> _blockingHandler, Consumer<Throwable> _errorHandler, Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler, Lock lock) {
        Optional.ofNullable(lock).ifPresent(lck -> lck.release());
        handleErrorExecution(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, Future.failedFuture("circuit open").cause());
    }

    public static <T> void executeDefault(String _methodId, ThrowableSupplier<T> _supplier, Future<T> _blockingHandler, Consumer<Throwable> _errorHandler,
                                          Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler, Vertx vertx,
                                          int _retry, long _timeout, long _circuitBreakerTimeout, long _delay, Lock lock) {
        Optional.ofNullable(lock).ifPresent(lck -> lck.release());
        vertx.executeBlocking(bhandler -> {
            try {
                executeDefaultState(_supplier, _blockingHandler, vertx, _timeout);
                bhandler.complete();
            } catch (Throwable e) {
                executeLocked((lck, counter) ->
                        counter.decrementAndGet(valHandler -> {
                            if (valHandler.succeeded()) {
                                handleStatefulError(_methodId, _supplier, _blockingHandler, _errorHandler, _onFailureRespond,
                                        _errorMethodHandler, vertx, _retry, _timeout, _circuitBreakerTimeout, _delay, e, lck, counter, valHandler);
                                bhandler.complete();
                            } else {
                                releaseLockAndHandleError(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, valHandler.cause(), lck);
                                bhandler.complete();
                            }
                        }), _methodId, vertx, _blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, bhandler);
            }
        }, false, res -> {

        });
    }


    public static <T> void executeInitialState(String _methodId, ThrowableSupplier<T> _supplier, Future<T> _blockingHandler, Consumer<Throwable> _errorHandler, Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler,
                                               Vertx vertx, int _retry, long _timeout, long _circuitBreakerTimeout, long _delay, Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(_retry + 1).longValue(), rHandler -> {
            executeDefault(_methodId, _supplier, _blockingHandler, _errorHandler, _onFailureRespond,
                    _errorMethodHandler, vertx, _retry, _timeout, _circuitBreakerTimeout, _delay, lock);
        });
    }

    public static <T> void releaseLockAndHandleError(Future<T> _blockingHandler, Consumer<Throwable> _errorHandler, Function<Throwable, T> _onFailureRespond,
                                                     Consumer<Throwable> _errorMethodHandler, Throwable cause, Lock lock) {
        Optional.ofNullable(lock).ifPresent(lck -> lck.release());
        handleErrorExecution(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, cause);
    }

    public static <T> void handleErrorExecution(Future<T> _blockingHandler, Consumer<Throwable> _errorHandler, Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler, Throwable cause) {
        T result = null;
        result = handleError(result, _errorHandler, _onFailureRespond, _errorMethodHandler, cause);
        if (!_blockingHandler.isComplete()) _blockingHandler.complete(result);
    }

    public static <T> void handleStatefulError(String _methodId, ThrowableSupplier<T> _supplier, Future<T> _blockingHandler, Consumer<Throwable> _errorHandler, Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler, Vertx vertx, int _retry, long _timeout, long _circuitBreakerTimeout, long _delay, Throwable e, Lock lck, Counter counter, AsyncResult<Long> valHandler) {
        //////////////////////////////////////////
        long count = valHandler.result();
        if (count <= 0) {
            setCircuitBreakerReleaseTimer(vertx, _retry, _circuitBreakerTimeout, counter);
            openCircuitBreakerAndHandleError(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, vertx, e, lck, counter);
        } else {
            lck.release();
            ResponseUtil.handleError(_errorHandler, e);
            handleDelay(_delay);
            executeRetryAndCatchAsync(_methodId, _supplier, _blockingHandler, _errorHandler,
                    _onFailureRespond, _errorMethodHandler, vertx, _retry, _timeout, _circuitBreakerTimeout, _delay);
        }
        ////////////////////////////////////////
    }

    public static <T> void openCircuitBreakerAndHandleError(Future<T> _blockingHandler, Consumer<Throwable> _errorHandler, Function<Throwable, T> _onFailureRespond,
                                                            Consumer<Throwable> _errorMethodHandler, Vertx vertx, Throwable e, Lock lck, Counter counter) {
        counter.addAndGet(-1l, val -> {
            lck.release();
            vertx.executeBlocking(bhandler -> {
                T result = null;
                result = handleError(result, _errorHandler, _onFailureRespond, _errorMethodHandler, e);
                if (!_blockingHandler.isComplete()) _blockingHandler.complete(result);
            }, false, res -> {

            });
        });
    }

    public static void setCircuitBreakerReleaseTimer(Vertx vertx, int _retry, long _circuitBreakerTimeout, Counter counter) {
        // TODO should the counter executed with lock?
        vertx.setTimer(_circuitBreakerTimeout, timer -> {
            counter.addAndGet(Integer.valueOf(_retry + 1).longValue(), val -> {
            });
        });
    }

    public static <T> void executeDefaultState(ThrowableSupplier<T> _supplier, Future<T> _blockingHandler, Vertx vertx, long _timeout) throws Throwable {
        T result = null;
        if (_timeout > 0L) {
            result = executeWithTimeout(_supplier, vertx, _timeout);
        } else {
            result = _supplier.get();
        }
        if (!_blockingHandler.isComplete()) _blockingHandler.complete(result);
    }

    public static <T> T executeWithTimeout(ThrowableSupplier<T> _supplier, Vertx vertx, long _timeout) throws Throwable {
        T result;
        final CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        vertx.executeBlocking((innerHandler) -> {
            try {
                timeoutFuture.complete(_supplier.get());
            } catch (Throwable throwable) {
                timeoutFuture.obtrudeException(throwable);
            }
        }, false, (val) -> {
        });

        try {
            result = timeoutFuture.get(_timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeout) {
            throw new TimeoutException("operation _timeout");
        }

        return result;
    }

    public static <T> void executeStateless(ThrowableSupplier<T> _supplier, Future<T> _blockingHandler, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond,
                                            Consumer<Throwable> errorMethodHandler, Vertx vertx, int _retry, long timeout, long delay) {
        T result = null;
        boolean errorHandling = false;
        while (_retry >= 0) {
            errorHandling = false;
            try {
                if (timeout > 0L) {
                    result = executeWithTimeout(_supplier, vertx, timeout);
                    _retry = -1;
                } else {
                    result = _supplier.get();
                    _retry = -1;
                }

            } catch (Throwable e) {
                _retry--;
                if (_retry < 0) {
                    result = handleError(result, errorHandler, onFailureRespond, errorMethodHandler, e);
                    errorHandling = true;
                } else {
                    ResponseUtil.handleError(errorHandler, e);
                    handleDelay(delay);
                }
            }
        }
        if (!errorHandling || errorHandling && result != null) {
            if (!_blockingHandler.isComplete()) _blockingHandler.complete(result);
        }
    }


    private static void handleDelay(long delay) {
        try {
            if (delay > 0L) Thread.sleep(delay);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }


    public static <T> T handleError(T result, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
        if (onFailureRespond != null) {
            result = onFailureRespond.apply(e);
        }
        if (errorHandler == null && onFailureRespond == null) {
            errorMethodHandler.accept(e); // TODO switch to function to return true if an error method was executed, no if no error method is available
            return null;

        }
        return result;
    }

    private static <T, U> void executeLocked(LockedConsumer consumer, String _methodId, Vertx vertx, Future<T> _blockingHandler, Consumer<Throwable> _errorHandler,
                                             Function<Throwable, T> _onFailureRespond, Consumer<Throwable> _errorMethodHandler, Future<U> blockingCodeHandler) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(_methodId, 2000, lockHandler -> {
            final Lock lock = lockHandler.result();
            if (lockHandler.succeeded()) {
                sharedData.getCounter(_methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        consumer.execute(lock, resultHandler.result());
                    } else {
                        releaseLockAndHandleError(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, resultHandler.cause(), lock);
                        Optional.ofNullable(blockingCodeHandler).ifPresent(bhandler -> bhandler.complete());
                    }
                });
            } else {
                handleErrorExecution(_blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, lockHandler.cause());
                Optional.ofNullable(blockingCodeHandler).ifPresent(bhandler -> bhandler.complete());
            }

        });
    }


    private interface LockedConsumer {
        void execute(Lock lock, Counter counter);
    }
}
