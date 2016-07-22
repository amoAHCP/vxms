package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.12.15.
 * The TargetType defines the target of the (blocking) response.
 */
public class WSResponseBlocking {
    protected final WebSocketEndpoint endpoint;
    protected final Vertx vertx;
    protected final WebSocketRegistry registry;
    protected final Consumer<Throwable> errorMethodHandler;

    protected WSResponseBlocking(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.errorMethodHandler = errorMethodHandler;
    }


    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking toAll() {
        return new ResponseTypeBlocking(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking toAllBut(WebSocketEndpoint... endpoints) {
        return new ResponseTypeBlocking(endpoints, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking reply() {
        return new ResponseTypeBlocking(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeBlocking to(WebSocketEndpoint... endpoints) {
        return new ResponseTypeBlocking(endpoints, vertx, CommType.TO, errorMethodHandler, registry);
    }


}
