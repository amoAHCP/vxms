package org.jacpfx.common;

import io.vertx.core.Future;

/**
 * Created by Andy Moncsek on 21.01.16.
 */
public interface ThrowableFutureConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param operationResult the input argument
     */
    void accept(Future<T> operationResult) throws Throwable;
}
