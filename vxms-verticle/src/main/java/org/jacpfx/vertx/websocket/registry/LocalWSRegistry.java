package org.jacpfx.vertx.websocket.registry;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 15.11.15.
 */
public class LocalWSRegistry implements  WSRegistry {

    private static final Logger log = LoggerFactory.getLogger(LocalWSRegistry.class);


    private final Vertx vertx;

    public LocalWSRegistry(Vertx vertx) {
        this.vertx = vertx;
    }




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
    public void findEndpointsAndExecute(WSEndpoint currentEndpoint, Consumer<WSEndpoint> onFinishRegistration) {
        findFilterAndExecute(currentEndpoint,(endpoint->true),onFinishRegistration);
    }

    public void findOtherEndpointsAndExecute(WSEndpoint currentEndpoint, Consumer<WSEndpoint> onFinishRegistration) {
        findFilterAndExecute(currentEndpoint,(endpoint->!endpoint.equals(currentEndpoint)),onFinishRegistration);
    }

    private void findFilterAndExecute(WSEndpoint currentEndpoint, Function<WSEndpoint,Boolean> filter,Consumer<WSEndpoint> onFinishRegistration) {
        final SharedData sharedData = this.vertx.sharedData();
        final LocalMap<String, byte[]> wsRegistry = sharedData.getLocalMap(WS_REGISTRY);
        Optional.ofNullable(getWSEndpointHolderFromSharedData(wsRegistry)).
                ifPresent(endpointHolder -> endpointHolder.
                        getAll().
                        stream().
                        filter(endpoint -> filter.apply(endpoint)).
                        filter(endpoint -> endpoint.getUrl().equals(currentEndpoint.getUrl())).
                        forEach(sameEndpoint -> onFinishRegistration.accept(sameEndpoint)));
    }


    @Override
    public void registerAndExecute(ServerWebSocket serverSocket, Consumer<WSEndpoint> onFinishRegistration) {
        final SharedData sharedData = this.vertx.sharedData();
        final LocalMap<String, byte[]> wsRegistry = sharedData.getLocalMap(WS_REGISTRY);
        final WSEndpointHolder holder = getWSEndpointHolderFromSharedData(wsRegistry);
        final String path = serverSocket.path();
        final WSEndpoint endpoint = new WSEndpoint(serverSocket.binaryHandlerID(), serverSocket.textHandlerID(), path);

        replaceOrAddEndpoint(wsRegistry, holder, endpoint);
        onFinishRegistration.accept(endpoint);
    }


    private void replaceOrAddEndpoint(LocalMap<String, byte[]> wsRegistry, WSEndpointHolder holder, WSEndpoint endpoint) {
        if (holder != null) {
            holder.add(endpoint);
            wsRegistry.replace(WS_ENDPOINT_HOLDER, serialize(holder));

        } else {
            final WSEndpointHolder holderTemp = new WSEndpointHolder();
            holderTemp.add(endpoint);
            wsRegistry.put(WS_ENDPOINT_HOLDER, serialize(holderTemp));
        }
    }


    private WSEndpointHolder getWSEndpointHolderFromSharedData(final LocalMap<String, byte[]> wsRegistry) {
        final byte[] holderPayload = wsRegistry.get(WS_ENDPOINT_HOLDER);
        if (holderPayload != null) {
            return (WSEndpointHolder) deserialize(holderPayload);
        }

        return null;
    }




}
