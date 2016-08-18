package org.jacpfx.vertx.registry;

import io.vertx.core.shareddata.Shareable;

import java.io.Serializable;

/**
 * Created by Andy Moncsek on 04.05.16.
 */
public class Root implements Serializable,Shareable {
    private final String action;
    private final Node node;
    private final Node prevNode;

    // For errors
    private final int errorCode;
    private final String message;
    private final String cause;
    private final int index;

    public Root(String action, Node node, Node prevNode, int errorCode, String message, String cause, int index) {
        this.action = action;
        this.node = node;
        this.prevNode = prevNode;
        this.errorCode = errorCode;
        this.message = message;
        this.cause = cause;
        this.index = index;
    }

    public Root() {
        this(null,null,null,0,null,null,0);
    }

    public Node getPrevNode() {
        return prevNode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getCause() {
        return cause;
    }

    public int getIndex() {
        return index;
    }

    public String getAction() {
        return action;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "Root{" +
                "action='" + action + '\'' +
                ", node=" + node +
                '}';
    }
}
