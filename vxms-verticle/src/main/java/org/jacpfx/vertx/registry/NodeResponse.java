package org.jacpfx.vertx.registry;

/**
 * Created by Andy Moncsek on 12.05.16.
 */
public class NodeResponse {
    private final Node node;
    private final boolean succeeded;
    private final Throwable throwable;

    public NodeResponse(Node node, boolean succeeded, Throwable throwable) {
        this.node = node;
        this.succeeded = succeeded;
        this.throwable = throwable;
    }

    public Node getNode() {
        return node;
    }

    public boolean succeeded() {
        return succeeded;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
