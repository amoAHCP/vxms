package or.jacpfx.spi;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Andy Moncsek on 23.06.16.
 */
public interface ServiceDiscoverySpi {

    void registerVerticle(Vertx vertx, Object verticleInstance, Future<Void> startFuture, JsonObject config);
}
