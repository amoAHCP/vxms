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

package org.jacpfx.vxms.rest.eventbus.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;

/**
 * Created by Andy Moncsek on 14.03.16. Defines an event-bus request as the beginning of your
 * (blocking) execution chain
 */
public class EventbusRequest {

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
   *     objects per instance
   * @param failure the vertx instance
   * @param errorMethodHandler the error-method handler
   * @param context the vertx routing context
   */
  public EventbusRequest(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context) {
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
    this.methodId = methodId;
  }

  /**
   * Send message and perform (blocking) task on reply
   *
   * @param targetId the target id to send to
   * @param message the message to send
   * @return the execution chain {@link EventbusResponse}
   */
  public EventbusResponse send(String targetId, Object message) {
    return new EventbusResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, targetId, message, null);
  }

  /**
   * @param targetId the target id to send to
   * @param message the message to send
   * @param options the delivery serverOptions for sending the message
   * @return the execution chain {@link EventbusResponse}
   */
  public EventbusResponse send(String targetId, Object message, DeliveryOptions options) {
    return new EventbusResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, targetId, message, options);
  }

  /**
   * Quickreply, send message over event-bus and pass the result directly to rest response
   *
   * @param targetId the target id to send to
   * @param message the message to send
   */
  public void sendAndRespondRequest(String targetId, Object message) {
    sendAndRespondRequest(targetId, message, new DeliveryOptions());
  }

  /**
   * Quickreply, send message over event-bus and pass the result directly to rest response
   *
   * @param targetId the target id to send to
   * @param message the message to send
   * @param options the event-bus delivery serverOptions
   */
  public void sendAndRespondRequest(String targetId, Object message, DeliveryOptions options) {
    final Vertx vertx = vxmsShared.getVertx();
    vertx
        .eventBus()
        .send(
            targetId,
            message,
            options != null ? options : new DeliveryOptions(),
            event -> {
              final HttpServerResponse response = context.response();
              if (event.failed()) {
                response.setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
              }
              Optional.ofNullable(event.result())
                  .ifPresent(
                      result ->
                          Optional.ofNullable(result.body())
                              .ifPresent(resp -> respond(response, resp)));
            });
  }

  protected void respond(HttpServerResponse response, Object resp) {
    if (resp instanceof String) {
      response.end((String) resp);
    } else if (resp instanceof byte[]) {
      response.end(Buffer.buffer((byte[]) resp));
    } else if (resp instanceof JsonObject) {
      response.end(JsonObject.class.cast(resp).encode());
    } else if (resp instanceof JsonArray) {
      response.end(JsonArray.class.cast(resp).encode());
    }
  }

  /**
   * Switch to blocking API
   *
   * @return the blocking chain {@link org.jacpfx.vxms.rest.eventbus.blocking.EventbusRequest}
   */
  public org.jacpfx.vxms.rest.eventbus.blocking.EventbusRequest blocking() {
    return new org.jacpfx.vxms.rest.eventbus.blocking.EventbusRequest(
        methodId, vxmsShared, failure, errorMethodHandler, context);
  }
}
