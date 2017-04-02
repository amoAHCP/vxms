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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by amo on 03.12.14.
 * The WebSocketEndpointHolder contains all registered WebSocket endpoint definitions. This class is a wrapper to be stored in shared data constructs of Vert.X.
 * This class will be serialised and accessed in one thread!!
 */
public class WebSocketEndpointHolder implements Serializable {

    private final List<WebSocketEndpoint> infos = new ArrayList<>();

    /**
     * Returns all registered Endpoints
     *
     * @return all WebSocket Endpoint definitions
     */
    public List<WebSocketEndpoint> getAll() {
        return Collections.unmodifiableList(infos);
    }

    /**
     * Removes an Endpoint
     *
     * @param info the Endpoint to remove
     */
    public void remove(final WebSocketEndpoint info) {
        final Optional<WebSocketEndpoint> first = getFirstMatch(info);
        first.ifPresent(infos::remove
        );
    }

    public void replace(final WebSocketEndpoint info) {
        remove(info);
        add(info);
    }

    /**
     * Add a WebSocket Endpoint
     *
     * @param info the Endpoint to add
     */
    public void add(final WebSocketEndpoint info) {
        final Optional<WebSocketEndpoint> first = getFirstMatch(info);
        if (!first.isPresent()) infos.add(info);
    }

    private Optional<WebSocketEndpoint> getFirstMatch(final WebSocketEndpoint info) {
        return infos.stream().filter(i -> i.equals(info)).findFirst();
    }


}
