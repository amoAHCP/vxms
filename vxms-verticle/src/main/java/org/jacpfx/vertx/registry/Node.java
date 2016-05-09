package org.jacpfx.vertx.registry;

import java.util.List;

/**
 * Created by Andy Moncsek on 04.05.16.
 */
public class Node {
    private final boolean dir;
    private final String key;
    private final String value;
    private final String expiration;
    private final int ttl;
    private final int modifiedIndex;
    private final int createdIndex;
    private final List<Node> nodes;

    public Node(boolean dir, String key, String value, String expiration, int ttl, int modifiedIndex, int createdIndex, List<Node> nodes) {
        this.dir = dir;
        this.key = key;
        this.value = value;
        this.expiration = expiration;
        this.ttl = ttl;
        this.modifiedIndex = modifiedIndex;
        this.createdIndex = createdIndex;
        this.nodes = nodes;
    }

    public Node() {
        this(false,null,null,null,0,0,0,null);
    }

    public boolean isDir() {
        return dir;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getExpiration() {
        return expiration;
    }

    public int getTtl() {
        return ttl;
    }

    public int getModifiedIndex() {
        return modifiedIndex;
    }

    public int getCreatedIndex() {
        return createdIndex;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Node{" +
                "dir=" + dir +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", expiration='" + expiration + '\'' +
                ", ttl=" + ttl +
                ", modifiedIndex=" + modifiedIndex +
                ", createdIndex=" + createdIndex +
                ", nodes=" + nodes +
                '}';
    }
}
