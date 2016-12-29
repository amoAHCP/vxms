package org.jacpfx.common;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.jacpfx.common.util.Serializer;

import java.io.IOException;

/**
 * Created by amo on 04.12.14.
 */
public class ServiceInfoDecoder implements MessageCodec<ServiceInfo, ServiceInfo> {
    @Override
    public void encodeToWire(Buffer buffer, ServiceInfo serviceInfo) {
        try {
            buffer.appendBytes(Serializer.serialize(serviceInfo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServiceInfo decodeFromWire(int pos, Buffer buffer) {
        try {
            return (ServiceInfo) Serializer.deserialize(buffer.getBytes());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ServiceInfo transform(ServiceInfo serviceInfo) {
        return serviceInfo;
    }

    @Override
    public String name() {
        return "ServiceInfoDecoder";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
