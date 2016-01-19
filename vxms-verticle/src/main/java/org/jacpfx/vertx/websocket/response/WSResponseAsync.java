package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.12.15.
 * The TargetType defines the target of the (async) response.
 */
public class WSResponseAsync {
    protected final WebSocketEndpoint endpoint;
    protected final Vertx vertx;
    protected final WebSocketRegistry registry;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final boolean async;

    protected WSResponseAsync(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler, boolean async) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.errorMethodHandler = errorMethodHandler;
        this.async = async;
    }


    /**
     * {@inheritDoc }
     */
    public ResponseTypeAsync toAll() {
        return new ResponseTypeAsync(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeAsync toAllBut(WebSocketEndpoint... endpoints) {
        return new ResponseTypeAsync(endpoints, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeAsync reply() {
        return new ResponseTypeAsync(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    public ResponseTypeAsync to(WebSocketEndpoint... endpoints) {
        return new ResponseTypeAsync(endpoints, vertx, CommType.TO, errorMethodHandler, registry);
    }


}
