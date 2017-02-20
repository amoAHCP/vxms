package org.jacpfx.eventbus;

import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.event.annotation.Consume;
import org.jacpfx.vertx.event.response.EventbusHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

/**
 * Created by amo on 20.02.17.
 */
@ServiceEndpoint(name = "/wsService", contextRoot = "/wsService", port = 9998)
public class WsServiceOne extends VxmsEndpoint {
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
    public static final int PORT = 9998;
    @Consume("/endpointOne")
    public void endpointOne(EventbusHandler reply) {
        System.out.println("wsEndpointOne: " + reply);
        reply.response().stringResponse((future) -> future.complete("test")).execute();
    }


}