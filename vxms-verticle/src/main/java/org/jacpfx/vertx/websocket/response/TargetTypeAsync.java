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
public class TargetTypeAsync extends TargetType {


    protected TargetTypeAsync(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler, boolean async) {
        super(endpoint, vertx, registry, errorMethodHandler, async);
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public ResponseTypeAsync toAll() {
        return new ResponseTypeAsync(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ResponseTypeAsync toAllBut(WebSocketEndpoint... endpoints) {
        return new ResponseTypeAsync(endpoints, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ResponseTypeAsync toCaller() {
        return new ResponseTypeAsync(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ResponseTypeAsync to(WebSocketEndpoint... endpoints) {
        return new ResponseTypeAsync(endpoints, vertx, CommType.TO, errorMethodHandler, registry);
    }


}
