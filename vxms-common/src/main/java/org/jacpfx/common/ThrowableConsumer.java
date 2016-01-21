package org.jacpfx.common;

/**
 * Created by Andy Moncsek on 21.01.16.
 */
public interface ThrowableConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws Throwable;
}
