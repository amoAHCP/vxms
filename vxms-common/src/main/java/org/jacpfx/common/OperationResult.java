package org.jacpfx.common;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.05.15.
 */
public class OperationResult {
    private final Operation operation;
    private final boolean succeeded;
    private final Throwable cause;


    /**
     * The default constructor
     *
     * @param operation The stream of ServiceInfos found
     * @param succeeded         the connection status
     * @param cause             The failure caus
     */
    public OperationResult(Operation operation, boolean succeeded, Throwable cause) {
        this.operation = operation;
        this.succeeded = succeeded;
        this.cause = cause;
    }


    /**
     * The ServiceInfo
     * @return Returns the  serviceInfo
     */
    public Operation getOperation() { return operation;}



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

    public static Consumer<OperationResult> onSuccessOp(Consumer<Operation> consumer, Consumer<Throwable> onFail) {
        return result -> {
            if (result.failed()) {
                onFail.accept(result.getCause());
            } else {
                consumer.accept(result.getOperation());
            }
        };
    }

}
