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
import io.vertx.ext.web.handler.CorsHandler;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class RestrictedBodyHandlingRouterConfig implements RouterConfiguration {

  public void corsHandler(Router router) {
    router.route().handler(CorsHandler.create("*").
        allowedMethod(io.vertx.core.http.HttpMethod.GET).
        allowedMethod(io.vertx.core.http.HttpMethod.POST).
        allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS).
        allowedMethod(io.vertx.core.http.HttpMethod.PUT).
        allowedMethod(io.vertx.core.http.HttpMethod.DELETE).
        allowedHeader("Content-Type").
        allowedHeader("X-Requested-With"));
  }

  public void bodyHandler(Router router) {

  }
}