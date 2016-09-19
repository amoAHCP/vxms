package org.jacpfx.common;

import java.util.stream.Stream;

/**
 * Created by amo on 19.09.16.
 */
public class ExecutionResult<T> {

    private final T result;
    private final boolean succeeded;
    private final Throwable cause;


    /**
     * The default constructor
     *
     * @param result
     * @param succeeded         the connection status
     * @param cause             The failure caus
     */
    public ExecutionResult(T result, boolean succeeded, Throwable cause) {
        this.result = result;
        this.succeeded = succeeded;
        this.cause = cause;
    }

    /**
     * The stream of services found
     *
     * @return a stream with requested ServiceInfos
     */
    public T getResult() {
        return result;
    }



    /**
     * The connection status
     *
     * @return true if connection to ServiceRepository succeeded
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * The connection status
     *
     * @return true if connection to ServiceRepository NOT succeeded
     */
    public boolean failed() {
        return !succeeded;
    }

    /**
     * Returns the failure cause
     *
     * @return The failure cause when connection to ServiceRegistry was not successful
     */
    public Throwable getCause() {
        return cause;
    }

}
