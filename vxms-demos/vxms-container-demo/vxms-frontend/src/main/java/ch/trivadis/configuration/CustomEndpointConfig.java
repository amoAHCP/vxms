package ch.trivadis.configuration;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.jacpfx.common.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class CustomEndpointConfig implements EndpointConfiguration {

    @Override
    public void staticHandler(Router router) {

        router.route().handler(StaticHandler.create());
    }

}
