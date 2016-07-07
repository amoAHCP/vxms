package or.jacpfx.spi;

import io.vertx.core.AbstractVerticle;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 23.06.16.
 */
public interface ServiceDiscoverySpi {

    void registerService(Runnable onSuccess, Consumer<Throwable> onFail, AbstractVerticle verticleInstance);

    void disconnect();
}
