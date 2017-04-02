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

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * Created by Andy Moncsek on 13.05.16.
 * A service node entry
 */
public class ServiceNode implements Serializable {

    private final String serviceId;
    private final String host;
    private final int port;
    private final boolean secure;
    private final URI uri;
    private final Map<String, String> metadata;


    /**
     * init the service node
     * @param serviceId the service id
     * @param host the hostname of the node
     * @param port the port number
     * @param secure true if secure
     * @param uri the node URI
     * @param metadata the additional node metadata
     */
    public ServiceNode(String serviceId, String host, int port, boolean secure, URI uri, Map<String, String> metadata) {
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.uri = uri;
        this.metadata = metadata;
    }

    /**
     * get the service id of the node
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * get the hostname of the node
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * get the port number of the node
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * true if node is secured
     * @return true if secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Returns the URI of the node
     * @return the node URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * return the  additional node metadata
     * @return the node properties
     */
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
