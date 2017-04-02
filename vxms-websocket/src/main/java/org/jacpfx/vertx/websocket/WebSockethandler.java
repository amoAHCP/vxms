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

package org.jacpfx.vertx.websocket;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import or.jacpfx.spi.WebSockethandlerSPI;
import org.jacpfx.vertx.websocket.registry.LocalWebSocketRegistry;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.WebSocketInitializer;

/**
 * Created by amo on 05.08.16.
 */
public class WebSockethandler implements WebSockethandlerSPI {
    @Override
    public void registerWebSocketHandler(HttpServer server, Vertx vertx, JsonObject config, AbstractVerticle service) {
        WebSocketInitializer.registerWebSocketHandler(server, vertx, initWebSocketRegistryInstance(vertx,config), config, service);
    }

    private WebSocketRegistry initWebSocketRegistryInstance(Vertx vertx,JsonObject config) {
        // TODO check clustering
        if (config.getBoolean("clustered", false)) {
            return null;
        } else {
            return new LocalWebSocketRegistry(vertx);
        }
    }
}
