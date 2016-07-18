package org.jacpfx.vertx.etcd.client;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

/**
 * Created by Andy Moncsek on 18.07.16.
 */
// TODO create Default impl, add to Annotation
public interface CustomConnectionOptions {

    default HttpClientOptions  getClientOptions(JsonObject config) {
        return new HttpClientOptions();
    }

}
