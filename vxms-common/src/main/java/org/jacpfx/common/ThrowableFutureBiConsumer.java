package org.jacpfx.common;

import io.vertx.core.Future;

/**
 * Created by Andy Moncsek on 21.01.16.
 */
public interface ThrowableFutureBiConsumer<H,T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param operationResult the input argument
     * @param handler the input argument
     */
    void accept(H handler, Future<T> operationResult) throws Throwable;
}
