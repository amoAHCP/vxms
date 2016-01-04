package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.11.15.
 * The WebSocketHandler delivers access to the Socket Endpoint, the payload and the response.
 */
public class WebSocketHandler {
    private final WebSocketEndpoint endpoint;
    private final Vertx vertx;
    private final WebSocketRegistry registry;
    private final Consumer<Throwable> errorMethodHandler;
    private final byte[] value;


    public WebSocketHandler(WebSocketRegistry registry, WebSocketEndpoint endpoint, byte[] value, Vertx vertx, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.value = value;
        this.errorMethodHandler = errorMethodHandler;
    }

    /**
     * Returns the Endpoint definition with URL and handler id
     *
     * @return {@see WSEndpoint}
     */
    public WebSocketEndpoint endpoint() {
        return this.endpoint;
    }

    /**
     * Returns the payload handler which gives you access to the payload transmitted
     *
     * @return {@see Payload}
     */
    public Payload payload() {
        return new Payload(value);
    }


    public TargetType response() {
        return new TargetType(endpoint, vertx, registry, errorMethodHandler, false);
    }


}
