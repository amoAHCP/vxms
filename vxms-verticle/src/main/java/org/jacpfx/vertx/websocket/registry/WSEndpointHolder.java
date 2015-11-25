package org.jacpfx.vertx.websocket.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by amo on 03.12.14.
 */
public class WSEndpointHolder implements Serializable {

    private final List<WSEndpoint> infos = new ArrayList<>();

    public List<WSEndpoint> getAll() {
        return Collections.unmodifiableList(infos);
    }

    public void remove(final WSEndpoint info) {
        final Optional<WSEndpoint> first = getFirstMatch(info);
        first.ifPresent(present ->
                        infos.remove(present)
        );
    }

    public void replace(final WSEndpoint info) {
        remove(info);
        add(info);
    }

    public void add(final WSEndpoint info) {
        final Optional<WSEndpoint> first = getFirstMatch(info);
        if(!first.isPresent())infos.add(info);
    }

    private Optional<WSEndpoint> getFirstMatch(final WSEndpoint info) {
        return infos.stream().filter(i -> i.equals(info)).findFirst();
    }


}
