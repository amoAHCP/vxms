package or.jacpfx.spi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Andy Moncsek on 23.06.16.
 */
public interface DiscoveryClientSpi<T> {

    T getClient(AbstractVerticle verticleInstance);
    T getClient(Vertx vertx,  JsonObject config);
}
