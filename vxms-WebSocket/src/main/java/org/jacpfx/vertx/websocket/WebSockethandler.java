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
