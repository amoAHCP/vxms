package org.jacpfx.common.exceptions;

/**
 * Created by Andy Moncsek on 30.11.15.
 */
public class EndpointExecutionException extends RuntimeException {

    public EndpointExecutionException() {
    }

    public EndpointExecutionException(String message) {
        super(message);
    }

    public EndpointExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndpointExecutionException(Throwable cause) {
        super(cause);
    }

    public EndpointExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
