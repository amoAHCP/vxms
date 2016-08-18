package org.jacpfx.vertx.registry;

import io.vertx.core.json.Json;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andy Moncsek on 12.05.16.
 */
public class NodeResponse {
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

    public ServiceNode getServiceNode() {
        if(!succeeded) throw new NodeNotFoundException(throwable);
        Collections.shuffle(nodes);
        Node selectedNode = nodes.get(0);
        NodeMetadata metadata = Json.decodeValue(selectedNode.getValue(),NodeMetadata.class);
        URI uri =  URI.create(metadata.getProtocol()+"://"+metadata.getHost()+":"+metadata.getPort()+metadata.getPath());
        return new ServiceNode(selectedNode.getKey(),metadata.getHost(),metadata.getPort(),metadata.isSecure(),uri,null);
    }

    public Node getNode() {
        if(!succeeded) throw new NodeNotFoundException(throwable);
        return nodes.get(0);
    }

    public boolean succeeded() {
        return succeeded;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
