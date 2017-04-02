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

package org.jacpfx.vertx.websocket.registry;

import java.io.Serializable;

/**
 * Represents the WebSocket session with it's binary- and texthandler id and it's url
 * Created by Andy Moncsek on 12.12.14.
 */
public class WebSocketEndpoint implements Serializable {
    private final String binaryHandlerId;
    private final String textHandlerId;
    private final String url;

    public WebSocketEndpoint(final String binaryHandlerId, final String textHandlerId, final String url) {
        this.binaryHandlerId = binaryHandlerId;
        this.textHandlerId = textHandlerId;
        this.url = url;
    }

    /**
     * Returns the binary handler id to send a binary message to the endpoint
     *
     * @return the binary handler id
     */
    public String getBinaryHandlerId() {
        return binaryHandlerId;
    }

    /**
     * Returns the text handler id to send a text message to the endpoint
     *
     * @return the text handler id
     */
    public String getTextHandlerId() {
        return textHandlerId;
    }

    /**
     * Returns the Endpoint URL
     *
     * @return the Endpoint URL
     */
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebSocketEndpoint)) return false;

        WebSocketEndpoint that = (WebSocketEndpoint) o;

        if (binaryHandlerId != null ? !binaryHandlerId.equals(that.binaryHandlerId) : that.binaryHandlerId != null)
            return false;
        if (textHandlerId != null ? !textHandlerId.equals(that.textHandlerId) : that.textHandlerId != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = binaryHandlerId != null ? binaryHandlerId.hashCode() : 0;
        result = 31 * result + (textHandlerId != null ? textHandlerId.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WebSocketEndpoint{" +
                "binaryHandlerId='" + binaryHandlerId + '\'' +
                ", textHandlerId='" + textHandlerId + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
