package org.jacpfx.vertx.registry;

/**
 * Created by Andy Moncsek on 12.05.16.
 */
public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException() {
    }

    public NodeNotFoundException(String message) {
        super(message);
    }

    public NodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public NodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
