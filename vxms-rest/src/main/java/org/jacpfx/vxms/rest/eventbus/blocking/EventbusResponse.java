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

package org.jacpfx.vxms.rest.eventbus.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import java.io.Serializable;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.rest.response.blocking.ExecuteRSByteResponse;
import org.jacpfx.vxms.rest.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vxms.rest.response.blocking.ExecuteRSStringResponse;
import org.jacpfx.vxms.rest.util.EventbusByteExecutionBlockingUtil;
import org.jacpfx.vxms.rest.util.EventbusObjectExecutionBlockingUtil;
import org.jacpfx.vxms.rest.util.EventbusStringExecutionBlockingUtil;

/** Created by Andy Moncsek on 14.03.16. Represents the start of a blocking execution chain */
public class EventbusResponse {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final RoutingContext context;
  private final String targetId;
  private final Object message;
  private final DeliveryOptions options;

  /**
   * Pass all parameters to execute the chain
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the vertx instance
   * @param errorMethodHandler the error-method handler
   * @param context the vertx routing context
   * @param targetId the event-bus message target-targetId
   * @param message the event-bus message
   * @param options the event-bus delivery serverOptions
   */
  public EventbusResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      String targetId,
      Object message,
      DeliveryOptions options) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
    this.targetId = targetId;
    this.message = message;
    this.options = options;
  }

  /**
   * Maps the event-bus response to a String response for the REST request
   *
   * @param stringFunction the function, that takes the response message from the event bus and that
   *     maps it to a valid response for the REST request
   * @return the execution chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse mapToStringResponse(
      ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
    return EventbusStringExecutionBlockingUtil.mapToStringResponse(
        methodId,
        targetId,
        message,
        options,
        stringFunction,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        null,
        null,
        null,
        null,
        null,
        0,
        0,
        0,
        0L,
        0L,
        0L);
  }

  /**
   * Maps the event-bus response to a byte response for the REST request
   *
   * @param byteFunction the function, that takes the response message from the event bus and that
   *     maps it to a valid response for the REST request
   * @return the execution chain {@link ExecuteRSByteResponse}
   */
  public ExecuteRSByteResponse mapToByteResponse(
      ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction) {

    return EventbusByteExecutionBlockingUtil.mapToByteResponse(
        methodId,
        targetId,
        message,
        options,
        byteFunction,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        null,
        null,
        null,
        null,
        null,
        0,
        0,
        0,
        0L,
        0L,
        0L);
  }

  /**
   * Maps the event-bus response to a byte response for the REST request
   *
   * @param objectFunction the function, that takes the response message from the event bus and that
   *     maps it to a valid response for the REST request
   * @param encoder the encoder to serialize your object response
   * @return the execution chain {@link ExecuteRSObjectResponse}
   */
  public ExecuteRSObjectResponse mapToObjectResponse(
      ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction,
      Encoder encoder) {

    return EventbusObjectExecutionBlockingUtil.mapToObjectResponse(
        methodId,
        targetId,
        message,
        options,
        objectFunction,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        null,
        null,
        encoder,
        null,
        null,
        0,
        0,
        0,
        0L,
        0L,
        0L);
  }
}
