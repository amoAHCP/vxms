package org.jacpfx.vertx.registry;

import io.vertx.core.json.Json;

import java.net.URI;
import java.util.Collections;
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
        Node selectedNode = null;
        if(nodes.size()>1) {
            Collections.shuffle(nodes);
            selectedNode = nodes.get(0);
        } else {
            selectedNode = node;
        }
        NodeMetadata metadata = Json.decodeValue(selectedNode.getValue(),NodeMetadata.class);
        URI uri =  URI.create(metadata.getProtocol()+"://"+metadata.getHost()+":"+metadata.getPort()+metadata.getPath());
        return new ServiceNode(selectedNode.getKey(),metadata.getHost(),metadata.getPort(),metadata.isSecure(),uri,null);
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
