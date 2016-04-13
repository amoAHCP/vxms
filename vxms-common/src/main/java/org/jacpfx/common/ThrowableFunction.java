package org.jacpfx.common;

/**
 * Created by Andy Moncsek on 12.04.16.
 */
public interface ThrowableFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws Throwable;
}
