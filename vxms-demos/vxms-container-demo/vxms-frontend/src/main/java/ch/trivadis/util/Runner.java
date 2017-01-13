package ch.trivadis.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Created by Andy Moncsek on 01.04.16.
 */
public class Runner {

    public static void run(DeploymentOptions options, Class clazz) {
        VertxOptions vOpts = new VertxOptions();
        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(clazz.getName(), options, handle -> {

                });
            }
        });
    }
}
