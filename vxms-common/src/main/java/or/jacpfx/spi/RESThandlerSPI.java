package or.jacpfx.spi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by amo on 05.08.16.
 * Defines SPI to bootstrap a vxms rest API
 */
public interface RESThandlerSPI {

    /**
     * initialize a rest API
     * @param vertx the vertx instance
     * @param router the vertx web router
     * @param service the verticle to be applied
     */
    void initRESTHandler(Vertx vertx, Router router, AbstractVerticle service);
}
