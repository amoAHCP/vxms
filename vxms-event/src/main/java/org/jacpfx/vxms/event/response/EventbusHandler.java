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

package org.jacpfx.vxms.event.response;

import io.vertx.core.eventbus.Message;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.event.eventbus.basic.EventbusBridgeRequest;

/**
 * Created by Andy Moncsek on 07.01.16. The EventbusHandler gives access to the {@link Message} ,
 * the {@link EventbusRequest} , the {@link EventbusResponse} and the {@link EventbusBridgeRequest}.
 */
public class EventbusHandler {

  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final Message<Object> message;
  private final String methodId;

  /**
   * The constructor initialize the Eventbus Handler
   *
   * @param methodId the method identifier
   * @param message the message to respond to
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution or messaging
   * @param errorMethodHandler the error-method handler
   */
  public EventbusHandler(
      String methodId,
      Message<Object> message,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler) {
    this.methodId = methodId;
    this.message = message;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
  }

  /**
   * Returns the message to respond to
   *
   * @return {@link Message}
   */
  public Message<Object> message() {
    return this.message;
  }

  /**
   * Returns the wrapped message to get access to message body
   *
   * @return {@link EventbusRequest}
   */
  public EventbusRequest request() {
    return new EventbusRequest(message);
  }

  /**
   * Starts the response chain to respond to message
   *
   * @return {@link EventbusResponse}
   */
  public EventbusResponse response() {
    return new EventbusResponse(methodId, message, vxmsShared, failure, errorMethodHandler);
  }

  /**
   * Starts the event-bus bridge chain to send a message and to use the response of this message to
   * create the main response
   *
   * @return {@link EventbusBridgeRequest}
   */
  public EventbusBridgeRequest eventBusRequest() {
    return new EventbusBridgeRequest(methodId, message, vxmsShared, failure, errorMethodHandler);
  }
}
