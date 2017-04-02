/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vertx.registry.nodes;

import io.vertx.core.shareddata.Shareable;

import java.io.Serializable;

/**
 * Created by Andy Moncsek on 04.05.16.
 * A root node in service discovery.
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

    /**
     * a (possible) node in service discovery
     * @return the previous node
     */
    public Node getPrevNode() {
        return prevNode;
    }

    /**
     * The error code when deiscovery fails
     * @return the error code
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * The error message when discovery fails
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * the cause of the failure
     * @return the failure cause
     */
    public String getCause() {
        return cause;
    }

    /**
     * The index of the node
     * @return the node index
     */
    public int getIndex() {
        return index;
    }

    /**
     * The node action
     * @return the node action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get the node definition of the root element
     * @return the node
     */
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
