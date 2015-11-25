package org.jacpfx.vertx.websocket.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by amo on 03.12.14.
 */
public class WebSocketEndpointHolder implements Serializable {

    private final List<WebSocketEndpoint> infos = new ArrayList<>();

    public List<WebSocketEndpoint> getAll() {
        return Collections.unmodifiableList(infos);
    }

    public void remove(final WebSocketEndpoint info) {
        final Optional<WebSocketEndpoint> first = getFirstMatch(info);
        first.ifPresent(present ->
                        infos.remove(present)
        );
    }

    public void replace(final WebSocketEndpoint info) {
        remove(info);
        add(info);
    }

    public void add(final WebSocketEndpoint info) {
        final Optional<WebSocketEndpoint> first = getFirstMatch(info);
        if(!first.isPresent())infos.add(info);
    }

    private Optional<WebSocketEndpoint> getFirstMatch(final WebSocketEndpoint info) {
        return infos.stream().filter(i -> i.equals(info)).findFirst();
    }


}
