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

package org.jacpfx.vertx.rest.eventbus.blocking;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.web.RoutingContext;
import java.util.function.Consumer;
import org.jacpfx.common.VxmsShared;

/**
 * Created by Andy Moncsek on 14.03.16.
 * Defines an event-bus request as the beginning of your (blocking) execution chain
 */
public class EventbusBlockingRequest {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final RoutingContext context;

  /**
   * Pass all members to execute the chain
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure the vertx instance
   * @param errorMethodHandler the error-method handler
   * @param context the vertx routing context
   */
  public EventbusBlockingRequest(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
  }


  /**
   * Send message and perform (blocking) task on reply
   *
   * @param targetId the target id to send to
   * @param message the message to send
   * @return the execution chain {@link EventbusBlockingResponse}
   */
  public EventbusBlockingResponse send(String targetId, Object message) {
    return new EventbusBlockingResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        targetId,
        message,
        null);
  }

  /**
   * Send message and perform (blocking) task on reply
   *
   * @param targetId the target id to send to
   * @param message the message to send
   * @param options the delivery options for sending the message
   * @return the execution chain {@link EventbusBlockingResponse}
   */
  public EventbusBlockingResponse send(String targetId, Object message, DeliveryOptions options) {
    return new EventbusBlockingResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        targetId,
        message,
        options);
  }
}
