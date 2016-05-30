package org.jacpfx.vertx.registry;

import io.vertx.core.json.Json;

import java.net.URI;
import java.util.List;

/**
 * Created by Andy Moncsek on 12.05.16.
 */
public class NodeResponse {
    private final Node node;
    private final List<Node> nodes;
    private final String domain;
    private final boolean succeeded;
    private final Throwable throwable;

    public NodeResponse(Node node, List<Node> nodes, String domain, boolean succeeded, Throwable throwable) {
        this.node = node;
        this.succeeded = succeeded;
        this.throwable = throwable;
        this.nodes = nodes;
        this.domain = domain;
    }

    public ServiceNode getServiceNode() {
        // TODO add client side loadbalancing by iterating through NodeList
        NodeMetadata metadata = Json.decodeValue(node.getValue(),NodeMetadata.class);
        URI uri =  URI.create(metadata.getProtocol()+"://"+metadata.getHost()+":"+metadata.getPort()+metadata.getPath());
        return new ServiceNode(node.getKey(),metadata.getHost(),metadata.getPort(),metadata.isSecure(),uri,null);
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
