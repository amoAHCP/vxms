package org.jacpfx.vertx.registry;

import java.io.Serializable;

/**
 * Created by Andy Moncsek on 29.05.16.
 */
public class NodeMetadata implements Serializable{
    private final String path;
    private final String host;
    private final int port;
    private final String protocol;
    private final boolean secure;

    public NodeMetadata(String path, String host, int port,  boolean secure) {
        this.path = path;
        this.host = host;
        this.port = port;
        this.protocol = secure ? "https" : "http";
        this.secure = secure;
    }
    public NodeMetadata() {
        this(null,null,0,false);
    }
    public String getPath() {
        return path;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isSecure() {
        return secure;
    }
}
