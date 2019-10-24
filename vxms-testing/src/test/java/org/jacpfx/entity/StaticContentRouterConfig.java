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

package org.jacpfx.entity;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;

/** Created by Andy Moncsek on 18.02.16. */
public class StaticContentRouterConfig implements RouterConfiguration {

  public void staticHandler(Router router) {
    router.route("/static/*").handler(StaticHandler.create());
    System.out.println("-----");
    // Create a router endpoint for the static content.
    // router.route().handler(StaticHandler.create());
  }

  public BodyHandler bodyHandler() {
    return BodyHandler.create();
  }
}
