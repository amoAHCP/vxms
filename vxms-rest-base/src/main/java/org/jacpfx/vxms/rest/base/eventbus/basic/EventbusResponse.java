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

package org.jacpfx.vxms.rest.base.eventbus.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import java.io.Serializable;
import java.util.function.Consumer;

import org.jacpfx.vxms.rest.base.response.basic.ExecuteRSByteResponse;
import org.jacpfx.vxms.rest.base.response.basic.ExecuteRSObjectResponse;
import org.jacpfx.vxms.rest.base.response.basic.ExecuteRSStringResponse;
import org.jacpfx.vxms.rest.base.util.EventbusByteExecutionUtil;
import org.jacpfx.vxms.rest.base.util.EventbusObjectExecutionUtil;
import org.jacpfx.vxms.rest.base.util.EventbusStringExecutionUtil;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;

/** Created by Andy Moncsek on 14.03.16. Represents the start of a non-blocking execution chain */
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
   * @param failure the last exception
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
   * Map Response from event-bus call to REST response
   *
   * @param stringFunction pass io.vertx.core.AsyncResult and future to complete with a String
   * @return the response chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse mapToStringResponse(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction) {

    return EventbusStringExecutionUtil.mapToStringResponse(
        methodId,
        targetId,
        message,
        stringFunction,
        options,
        vxmsShared,
        failure,
        errorMethodHandler,
        context);
  }

  /**
   * Map Response from event-bus call to REST response
   *
   * @param byteFunction pass io.vertx.core.AsyncResult and future to complete with a byte[] array
   * @return the response chain {@link ExecuteRSByteResponse}
   */
  public ExecuteRSByteResponse mapToByteResponse(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction) {

    return EventbusByteExecutionUtil.mapToByteResponse(
        methodId,
        targetId,
        message,
        byteFunction,
        options,
        vxmsShared,
        failure,
        errorMethodHandler,
        context);
  }

  /**
   * Map Response from event-bus call to REST response
   *
   * @param objectFunction pass io.vertx.core.AsyncResult and future to complete with a Object
   * @param encoder the Object encoder
   * @return the response chain {@link ExecuteRSObjectResponse}
   */
  public ExecuteRSObjectResponse mapToObjectResponse(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> objectFunction,
      Encoder encoder) {

    return EventbusObjectExecutionUtil.mapToObjectResponse(
        methodId,
        targetId,
        message,
        objectFunction,
        options,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        encoder);
  }
}
