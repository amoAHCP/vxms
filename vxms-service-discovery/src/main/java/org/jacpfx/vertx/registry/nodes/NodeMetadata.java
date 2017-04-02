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

/**
 * Created by Andy Moncsek on 29.05.16.
 * The internal node metadata of the service, used for registration of the current instance
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

    /**
     * the service path
     * @return the service path
     */
    public String getPath() {
        return path;
    }

    /**
     * The host name/ ip of the service
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * the port number of the service
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * returns the protocol prefix
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    public boolean isSecure() {
        return secure;
    }
}
