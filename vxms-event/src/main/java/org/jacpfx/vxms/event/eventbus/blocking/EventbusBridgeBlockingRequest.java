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

package org.jacpfx.vxms.event.eventbus.blocking;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;

/**
 * Created by Andy Moncsek on 14.03.16. Defines an event-bus request as the beginning of your
 * (blocking) execution chain
 */
public class EventbusBridgeBlockingRequest {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final Message<Object> requestmessage;

  /**
   * Pass all members to execute the chain
   *
   * @param methodId the method identifier
   * @param requestmessage the message to responde
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the vertx instance
   * @param errorMethodHandler the error-method handler
   */
  public EventbusBridgeBlockingRequest(
      String methodId,
      Message<Object> requestmessage,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.requestmessage = requestmessage;
  }

  /**
   * Send message and perform (blocking) task on reply
   *
   * @param id the target id to send to
   * @param message the message to send
   * @return the execution chain {@link EventbusBridgeBlockingResponse}
   */
  public EventbusBridgeBlockingResponse send(String id, Object message) {
    return new EventbusBridgeBlockingResponse(
        methodId, requestmessage, vxmsShared, failure, errorMethodHandler, id, message, null);
  }

  /**
   * Send message and perform (blocking) task on reply
   *
   * @param id the target id to send to
   * @param message the message to send
   * @param requestOptions the delivery serverOptions for the event bus request
   * @return the execution chain {@link EventbusBridgeBlockingResponse}
   */
  public EventbusBridgeBlockingResponse send(
      String id, Object message, DeliveryOptions requestOptions) {
    return new EventbusBridgeBlockingResponse(
        methodId,
        requestmessage,
        vxmsShared,
        failure,
        errorMethodHandler,
        id,
        message,
        requestOptions);
  }
}
