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

package org.jacpfx.vxms.common.configuration;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;

/**
 * Created by Andy Moncsek on 18.02.16.
 * The endpoint configuration interface
 */
public interface EndpointConfiguration {

  /**
   * define a corse handler for your service
   *
   * @param router {@link Router}
   */
  default void corsHandler(Router router) {

  }

  /**
   * Define a body handler for your service, a body handler is always set by default
   *
   * @param router {@link Router}
   */
  default void bodyHandler(Router router) {
    router.route().handler(BodyHandler.create());
  }

  /**
   * Define a coockie handler for your service, a cookie handler is always defined by default
   *
   * @param router {@link Router}
   */
  default void cookieHandler(Router router) {
    router.route().handler(CookieHandler.create());
  }

  /**
   * Define the static handler
   *
   * @param router {@link Router}
   */
  default void staticHandler(Router router) {
  }

  /**
   * Define a Session handler
   *
   * @param vertx {@link Vertx}
   * @param router {@link Router}
   */
  default void sessionHandler(Vertx vertx, Router router) {

  }

  /**
   * define custom Routes, not covered by defined rest methods
   *
   * @param vertx {@link Vertx}
   * @param router {@link Router}
   * @param secure true if ssl is enabled
   * @param host the defined host
   * @param port the defined port number
   */
  default void customRouteConfiguration(Vertx vertx, Router router, boolean secure, String host,
      int port) {

  }
}
