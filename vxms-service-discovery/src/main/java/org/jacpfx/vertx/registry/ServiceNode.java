package org.jacpfx.vertx.registry;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * Created by Andy Moncsek on 13.05.16.
 */
public class ServiceNode implements Serializable {

    private final String serviceId;
    private final String host;
    private final int port;
    private final boolean secure;
    private final URI uri;
    private final Map<String, String> metadata;


    public ServiceNode(String serviceId, String host, int port, boolean secure, URI uri, Map<String, String> metadata) {
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.uri = uri;
        this.metadata = metadata;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }


    @Override
    public String toString() {
        return "ServiceNode{" +
                "serviceId='" + serviceId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", secure=" + secure +
                ", uri=" + uri +
                ", metadata=" + metadata +
                '}';
    }
}
