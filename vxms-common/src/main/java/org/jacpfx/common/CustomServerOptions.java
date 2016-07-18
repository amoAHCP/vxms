package org.jacpfx.common;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

/**
 * Created by Andy Moncsek on 18.07.16.
 */
public interface CustomServerOptions {

    default HttpServerOptions getOptions(JsonObject config) {
        return new HttpServerOptions();
    }
}
