package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 02.05.16.
 */
public class WSEventBusRequest {
    private final WebSocketEndpoint endpoint;
    private final Vertx vertx;
    private final WebSocketRegistry registry;
    private final Consumer<Throwable> errorMethodHandler;
    private final byte[] value;


    public WSEventBusRequest(WebSocketRegistry registry, WebSocketEndpoint endpoint, byte[] value, Vertx vertx, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.value = value;
        this.errorMethodHandler = errorMethodHandler;
    }

}
