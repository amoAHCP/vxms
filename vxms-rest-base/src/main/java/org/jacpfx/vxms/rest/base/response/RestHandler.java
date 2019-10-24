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

package org.jacpfx.vxms.rest.base.response;

import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.rest.base.eventbus.basic.EventbusRequest;

/**
 * Created by Andy Moncsek on 07.01.16. The RestHandler gives access to the {@link RoutingContext} ,
 * the {@link RESTRequest} , the {@link RESTResponse} and the {@link EventbusRequest}. It is the
 * Entry point to the fluent API to perform tasks and create responses.
 */
public class RestHandler {

  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final RoutingContext context;
  private final String methodId;

  /**
   * The constructor initialize the Rest handler
   *
   * @param methodId the method identifier
   * @param context the vertx routing context
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution or messaging
   * @param errorMethodHandler the error-method handler
   */
  public RestHandler(
      String methodId,
      RoutingContext context,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler) {
    this.methodId = methodId;
    this.context = context;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
  }

  /**
   * Returns the Vert.x http Routing context
   *
   * @return {@link RoutingContext}
   */
  public RoutingContext context() {
    return this.context;
  }

  /**
   * Returns the data wrapper to access the http request, attributes and parameters.
   *
   * @return {@link RESTRequest}
   */
  public RESTRequest request() {
    return new RESTRequest(context);
  }

  /**
   * Starts the fluent API handling to execute tasks and create a response
   *
   * @return {@link RESTResponse}
   */
  public RESTResponse response() {
    return new RESTResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, new HashMap<>());
  }

  /**
   * Starts the fluent API to create an Event bus request, to perform a task and to create a
   * response
   *
   * @return {@link EventbusRequest}
   */
  public EventbusRequest eventBusRequest() {
    return new EventbusRequest(methodId, vxmsShared, failure, errorMethodHandler, context);
  }
}
