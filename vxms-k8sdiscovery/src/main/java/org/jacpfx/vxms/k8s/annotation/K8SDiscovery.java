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

package org.jacpfx.vxms.k8s.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
/**
 * The Kubernetes discovery annotation
 */
public @interface K8SDiscovery {

  /**
   * The user to access the master API
   *
   * @return the name of the user to access the master API
   */
  String user() default "";

  /**
   * The password to access the master API
   *
   * @return The API password
   */
  String password() default "";

  /**
   * The API token
   *
   * @return the API token
   */
  String api_token() default "";

  /**
   * The Kubernetes master URL
   *
   * @return the master url
   */
  String master_url() default "https://kubernetes.default.svc";

  /**
   * The namespace where to do the discovery
   *
   * @return the namespace where to discover
   */
  String namespace() default "default";
}
