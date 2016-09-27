package org.jacpfx.common;

import io.vertx.core.Future;

/**
 * Created by Andy Moncsek on 21.01.16.
 */
public interface ThrowableErrorConsumer<T, R> {

    /**
     * Performs this operation on the given argument.
     *
     * @param error,          the error
     * @param operationResult the input argument
     */
    void accept(T error, Future<R> operationResult) throws Throwable;
}
