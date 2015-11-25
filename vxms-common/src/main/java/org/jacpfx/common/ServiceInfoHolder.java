package org.jacpfx.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by amo on 03.12.14.
 */
public class ServiceInfoHolder implements Serializable {



    private final List<ServiceInfo> infos;
    public ServiceInfoHolder() {
        this.infos = new ArrayList<>();
    }
    public ServiceInfoHolder(final List<ServiceInfo> infos) {
        this.infos = infos;
    }
    public List<ServiceInfo> getAll() {
        return Collections.unmodifiableList(infos);
    }

    public void remove(final ServiceInfo info) {
        final Optional<ServiceInfo> first = infos.stream().filter(i -> i.getServiceName().equals(info.getServiceName())).findFirst();
        first.ifPresent(present ->
                        infos.remove(present)
        );
    }

    public void replace(final ServiceInfo info) {
        remove(info);
        add(info);
    }

    public void add(final ServiceInfo info) {
        final Optional<ServiceInfo> first = infos.stream().filter(i -> i.getServiceName().equals(info.getServiceName())).findFirst();
        if(!first.isPresent())infos.add(info);
    }

    public JsonObject getServiceInfo() {
        final JsonArray all = new JsonArray();
        infos.forEach(handler -> all.add(ServiceInfo.buildFromServiceInfo(handler)));
        return new JsonObject().put("services", all);
    }
}
