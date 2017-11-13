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

package org.jacpfx.vxms.event.eventbus.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.io.Serializable;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusBasicByteResponse;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusBasicObjectResponse;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusBasicStringResponse;
import org.jacpfx.vxms.event.util.EventbusByteExecutionUtil;
import org.jacpfx.vxms.event.util.EventbusObjectExecutionUtil;
import org.jacpfx.vxms.event.util.EventbusStringExecutionUtil;

/** Created by Andy Moncsek on 14.03.16. Represents the start of a non- blocking execution chain */
public class EventbusBridgeResponse {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final Message<Object> requestmessage;
  private final String targetId;
  private final Object message;
  private final DeliveryOptions requestOptions;

  /**
   * Pass all parameters to execute the chain
   *
   * @param methodId the method identifier
   * @param requestmessage the message to responde
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the last failure
   * @param errorMethodHandler the error-method handler
   * @param targetId the event-bus message target-targetId
   * @param message the event-bus message
   * @param requestOptions the event-bus delivery serverOptions
   */
  public EventbusBridgeResponse(
      String methodId,
      Message<Object> requestmessage,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      String targetId,
      Object message,
      DeliveryOptions requestOptions) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.requestmessage = requestmessage;
    this.targetId = targetId;
    this.message = message;
    this.requestOptions = requestOptions;
  }

  /**
   * Map Response from event-bus call to REST response
   *
   * @param stringFunction pass io.vertx.core.AsyncResult and future to complete with a String
   * @return the response chain {@link ExecuteEventbusBasicStringResponse}
   */
  public ExecuteEventbusBasicStringResponse mapToStringResponse(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction) {

    return EventbusStringExecutionUtil.mapToStringResponse(
        methodId,
        targetId,
        message,
        stringFunction,
        requestOptions,
        vxmsShared,
        failure,
        errorMethodHandler,
        requestmessage,
        null,
        null,
        null,
        null,
        0,
        0l,
        0l);
  }

  /**
   * Map Response from event-bus call to REST response
   *
   * @param byteFunction pass io.vertx.core.AsyncResult and future to complete with a byte[] array
   * @return the response chain {@link ExecuteEventbusBasicByteResponse}
   */
  public ExecuteEventbusBasicByteResponse mapToByteResponse(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction) {

    return EventbusByteExecutionUtil.mapToByteResponse(
        methodId,
        targetId,
        message,
        byteFunction,
        requestOptions,
        vxmsShared,
        failure,
        errorMethodHandler,
        requestmessage,
        null,
        null,
        null,
        null,
        0,
        0l,
        0l);
  }

  /**
   * Map Response from event-bus call to REST response
   *
   * @param objectFunction pass io.vertx.core.AsyncResult and future to complete with a Object
   * @param encoder the Object encoder
   * @return the response chain {@link ExecuteEventbusBasicObjectResponse}
   */
  public ExecuteEventbusBasicObjectResponse mapToObjectResponse(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> objectFunction,
      Encoder encoder) {

    return EventbusObjectExecutionUtil.mapToObjectResponse(
        methodId,
        targetId,
        message,
        objectFunction,
        requestOptions,
        vxmsShared,
        failure,
        errorMethodHandler,
        requestmessage,
        null,
        encoder,
        null,
        null,
        null,
        0,
        0l,
        0l);
  }
}
