package org.jacpfx.vertx.websocket.registry;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 15.11.15.
 */
public class LocalWebSocketRegistry implements WebSocketRegistry {


    private final Vertx vertx;

    public LocalWebSocketRegistry(Vertx vertx) {
        this.vertx = vertx;
    }


    /**
     * Removes a WebSocket Endpoint from the {@see WebSocketEndpointHolder} and executes th provided method.
     *
     * @param serverSocket   the WebSocket to remove
     * @param onFinishRemove the method to execute after remove
     */
    @Override
    public void removeAndExecuteOnClose(ServerWebSocket serverSocket, Runnable onFinishRemove) {
        final SharedData sharedData = this.vertx.sharedData();
        final LocalMap<String, byte[]> wsRegistry = sharedData.getLocalMap(WS_REGISTRY);
        Optional.ofNullable(getWSEndpointHolderFromSharedData(wsRegistry)).
                ifPresent(endpointHolder -> endpointHolder.
                        getAll().
                        stream().
                        filter(endpoint -> endpoint.getBinaryHandlerId().equals(serverSocket.binaryHandlerID()) && endpoint.getTextHandlerId().equals(serverSocket.textHandlerID())).
                        findFirst().
                        ifPresent(endpoint -> {
                            endpointHolder.remove(endpoint);
                            wsRegistry.replace(WS_ENDPOINT_HOLDER, serialize(endpointHolder));
                            onFinishRemove.run();

                        }));
    }

    @Override
    public void findEndpointsByURLAndExecute(WebSocketEndpoint currentEndpoint, Consumer<WebSocketEndpoint> executeOnMatch) {
        findEndpointsAndExecute(currentEndpoint, (endpoint -> true), executeOnMatch);
    }

    @Override
    public void findEndpointsAndExecute(WebSocketEndpoint currentEndpoint, Function<WebSocketEndpoint, Boolean> filter, Consumer<WebSocketEndpoint> executeOnMatch) {
        final SharedData sharedData = this.vertx.sharedData();
        final LocalMap<String, byte[]> wsRegistry = sharedData.getLocalMap(WS_REGISTRY);
        Optional.ofNullable(getWSEndpointHolderFromSharedData(wsRegistry)).
                ifPresent(endpointHolder -> filterAndExecute(currentEndpoint, filter, executeOnMatch, endpointHolder));
    }

    /**
     * Filters all Endpoints by provided filter and with the same URL (same endpoint method)
     *
     * @param currentEndpoint the Endpoint providing the URL
     * @param filter          the filter
     * @param executeOnMatch  method to execute on match
     * @param endpointHolder  the Endpoint holder
     */
    private void filterAndExecute(WebSocketEndpoint currentEndpoint, Function<WebSocketEndpoint, Boolean> filter, Consumer<WebSocketEndpoint> executeOnMatch, WebSocketEndpointHolder endpointHolder) {
        endpointHolder.
                getAll().
                stream().
                filter(endpoint -> filter.apply(endpoint)).
                filter(endpoint -> endpoint.getUrl().equals(currentEndpoint.getUrl())).
                forEach(sameEndpoint -> executeOnMatch.accept(sameEndpoint));
    }


    @Override
    public void registerAndExecute(ServerWebSocket serverSocket, Consumer<WebSocketEndpoint> onFinishRegistration) {
        final SharedData sharedData = this.vertx.sharedData();
        final LocalMap<String, byte[]> wsRegistry = sharedData.getLocalMap(WS_REGISTRY);
        final WebSocketEndpointHolder holder = getWSEndpointHolderFromSharedData(wsRegistry);
        final String path = serverSocket.path();
        final WebSocketEndpoint endpoint = new WebSocketEndpoint(serverSocket.binaryHandlerID(), serverSocket.textHandlerID(), path);

        replaceOrAddEndpoint(wsRegistry, holder, endpoint);
        onFinishRegistration.accept(endpoint);
    }


    private void replaceOrAddEndpoint(LocalMap<String, byte[]> wsRegistry, WebSocketEndpointHolder holder, WebSocketEndpoint endpoint) {
        if (holder != null) {
            holder.add(endpoint);
            wsRegistry.replace(WS_ENDPOINT_HOLDER, serialize(holder));

        } else {
            final WebSocketEndpointHolder holderTemp = new WebSocketEndpointHolder();
            holderTemp.add(endpoint);
            wsRegistry.put(WS_ENDPOINT_HOLDER, serialize(holderTemp));
        }
    }


    private WebSocketEndpointHolder getWSEndpointHolderFromSharedData(final LocalMap<String, byte[]> wsRegistry) {
        final byte[] holderPayload = wsRegistry.get(WS_ENDPOINT_HOLDER);
        if (holderPayload != null) {
            return (WebSocketEndpointHolder) deserialize(holderPayload);
        }

        return null;
    }


}
