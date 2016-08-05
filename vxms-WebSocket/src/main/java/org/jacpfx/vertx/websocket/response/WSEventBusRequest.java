package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 02.05.16.
 */
public class WSEventBusRequest {
    protected final WebSocketEndpoint endpoint;
    protected final Vertx vertx;
    protected final WebSocketRegistry registry;
    protected final Consumer<Throwable> errorMethodHandler;


    public WSEventBusRequest(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.errorMethodHandler = errorMethodHandler;
    }
}
