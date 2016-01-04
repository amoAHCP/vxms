package org.jacpfx.common;

/**
 * Created by Andy Moncsek on 27.11.15.
 */
public interface ThrowableSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws Throwable;
}
