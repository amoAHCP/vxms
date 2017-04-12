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

package org.jacpfx.vertx.etcd.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Etcd service discovery annotation. Annotate a vxms verticle to activate verticle registration and
 * to allow DiscoveryClient injection. Created by Andy Moncsek on 13.06.16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EtcdClient {

  /**
   * the etcd connection port
   *
   * @return the port number
   */
  int port() default 4001;

  /**
   * the etcd host
   *
   * @return the host name / IP
   */
  String host() default "127.0.0.1";


  /**
   * ttl in seconds
   *
   * @return ttl sec. value
   */
  int ttl() default 30;

  /**
   * The domain name where to register
   *
   * @return the domain name
   */
  String domain() default "default";

  /**
   * Overwrite host name for service location
   *
   * @return the host name to be registered
   */
  String exportedHost() default "";

  /**
   * Overwrite exported port
   *
   * @return the port number to be registered
   */
  int exportedPort() default 0;


  /**
   * Define custom http client options
   *
   * @return the server options
   */
  Class<? extends CustomConnectionOptions> options() default DefaultConnectionOptions.class;
}
