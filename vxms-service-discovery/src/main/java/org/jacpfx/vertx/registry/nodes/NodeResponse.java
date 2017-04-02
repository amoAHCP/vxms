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

import io.vertx.core.json.Json;
import org.jacpfx.vertx.registry.error.NodeNotFoundException;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Represents a response for a service lookup
 * Created by Andy Moncsek on 12.05.16.
 */
public class NodeResponse {
    public static final String PORT_DELIMITER = ":";
    public static final String PROTOCOL_DELIMITER = "://";
    private final List<Node> nodes;
    private final String domain;
    private final boolean succeeded;
    private final Throwable throwable;

    public NodeResponse(List<Node> nodes, String domain, boolean succeeded, Throwable throwable) {
        this.succeeded = succeeded;
        this.throwable = throwable;
        this.nodes = nodes;
        this.domain = domain;
    }

    /**
     * The service node provides access to a node instance
     *
     * @return the service node {@link ServiceNode}
     */
    public ServiceNode getServiceNode() {
        if (!succeeded) throw new NodeNotFoundException(throwable);
        Collections.shuffle(nodes);
        Node selectedNode = nodes.get(0);
        NodeMetadata metadata = Json.decodeValue(selectedNode.getValue(), NodeMetadata.class);
        // TODO check for context root correct format
        URI uri = URI.create(metadata.getProtocol() + PROTOCOL_DELIMITER + metadata.getHost() + PORT_DELIMITER + metadata.getPort() + metadata.getPath());
        return new ServiceNode(selectedNode.getKey(), metadata.getHost(), metadata.getPort(), metadata.isSecure(), uri, null);
    }

    /**
     * The node with all metadata to the service
     *
     * @return the {@link Node}
     */
    public Node getNode() {
        if (!succeeded) throw new NodeNotFoundException(throwable);
        return nodes.get(0);
    }

    /**
     * Check if discovery succeeded
     *
     * @return true if node was found
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * in case of not succeeded you get the Throwable
     *
     * @return the error
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * The domain name where the service is registered
     *
     * @return the service domain name
     */
    public String getDomain() {
        if (!succeeded) throw new NodeNotFoundException(throwable);
        return domain;
    }
}
