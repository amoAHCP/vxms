package org.jacpfx.config;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.util.KeyUtil;

import java.io.File;

/**
 * Created by amo on 11.08.16.
 */
public class CustomHTTPOptions implements CustomServerOptions {

    public HttpServerOptions getServerOptions(JsonObject config) {
        if (!new File(KeyUtil.DEMO_KEYSTTORE).exists()) {
            KeyUtil.generateKey(); // only for demo, create keystore
        }
        return new HttpServerOptions().
                setKeyStoreOptions(new JksOptions().setPath(KeyUtil.DEMO_KEYSTTORE).setPassword(KeyUtil.DEMO_PWD)).
                setSsl(true);
    }
}
