/*
 * Copyright [2017] [Andy Moncsek]
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

package org.jacpfx.vxms.frontend.configuration;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.jacpfx.vxms.common.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class CustomEndpointConfig implements EndpointConfiguration {

    @Override
    public void staticHandler(Router router) {

        router.route().handler(StaticHandler.create());
    }

}
