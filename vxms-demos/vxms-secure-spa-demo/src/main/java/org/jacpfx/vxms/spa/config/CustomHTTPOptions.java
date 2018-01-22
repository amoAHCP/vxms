/*
 * Copyright [2018] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.spa.config;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import org.jacpfx.vxms.common.CustomServerOptions;
import org.jacpfx.vxms.spa.util.KeyUtil;

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
