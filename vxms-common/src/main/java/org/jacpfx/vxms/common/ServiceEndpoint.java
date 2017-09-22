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

package org.jacpfx.vxms.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jacpfx.vxms.common.configuration.DefaultRouterConfiguration;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;

/**
 * Created by Andy Moncsek on 31.07.15. Defines an ServiceEndpoint and his metadata. A Class
 * Annotated with {@link ServiceEndpoint} must extend from ServiceVerticle or initialized statically with the factory
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceEndpoint {

  /**
   * The port number to listen
   *
   * @return The Endpoint Port
   */
  int port() default 8080;

  /**
   * The service name as identifier in distributed environments
   *
   * @return the service name
   */
  String name() default "";


  String contextRoot() default "/";

  /**
   * @return The host name to bind
   */
  String host() default "0.0.0.0";


  /**
   * Define custom http server options to enable e.g HTTPS.
   *
   * @return the http serverOptions {@link org.jacpfx.vxms.common.CustomServerOptions}
   */
  Class<? extends CustomServerOptions> serverOptions() default DefaultServerOptions.class;


  /**
   * The Class of type RouterConfiguration defining the custom Endpoint configuration
   *
   * @return Class implementing  {@link RouterConfiguration}
   */
  Class<? extends RouterConfiguration> routerConf() default DefaultRouterConfiguration.class;
}
