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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;

/**
 * Created by Andy Moncsek on 14.03.16. Defines an event-bus request as the beginning of your
 * execution chain
 */
public class EventbusBridgeRequest {

  private final String methodId;
  private final Message<Object> requestmessage;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;

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
  public EventbusBridgeRequest(
      String methodId,
      Message<Object> requestmessage,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler) {
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.requestmessage = requestmessage;
    this.methodId = methodId;
  }

  /**
   * Send message and perform task on reply
   *
   * @param id the target id to send to
   * @param message the message to send
   * @return the execution chain {@link EventbusBridgeResponse}
   */
  public EventbusBridgeResponse send(String id, Object message) {
    return new EventbusBridgeResponse(
        methodId, requestmessage, vxmsShared, failure, errorMethodHandler, id, message, null);
  }

  /**
   * Send message and perform task on reply
   *
   * @param id the target id to send to
   * @param message the message to send
   * @param requestOptions the delivery serverOptions for the event bus request
   * @return the execution chain {@link EventbusBridgeResponse}
   */
  public EventbusBridgeResponse send(String id, Object message, DeliveryOptions requestOptions) {
    return new EventbusBridgeResponse(
        methodId,
        requestmessage,
        vxmsShared,
        failure,
        errorMethodHandler,
        id,
        message,
        requestOptions);
  }

  /**
   * Send message and redirect the event bus response directly to the initial request
   *
   * @param id the target id to send to
   * @param message the message to send
   */
  public void sendAndRespondRequest(String id, Object message) {
    sendAndRespondRequest(id, message, new DeliveryOptions());
  }

  /**
   * Send message and redirect the event bus response directly to the initial request
   *
   * @param id the target id to send to
   * @param message the message to send
   * @param requestOptions the delivery serverOptions for the event bus request
   */
  public void sendAndRespondRequest(String id, Object message, DeliveryOptions requestOptions) {
    final Vertx vertx = vxmsShared.getVertx();
    vertx
        .eventBus()
        .send(
            id,
            message,
            requestOptions != null ? requestOptions : new DeliveryOptions(),
            event -> {
              if (event.failed()) {
                requestmessage.fail(
                    HttpResponseStatus.SERVICE_UNAVAILABLE.code(), event.cause().getMessage());
              }
              Optional.ofNullable(event.result())
                  .ifPresent(
                      result ->
                          Optional.ofNullable(result.body())
                              .ifPresent(resp -> respond(resp, requestOptions)));
            });
  }

  protected void respond(Object resp, DeliveryOptions options) {
    if (resp instanceof String) {
      if (options != null) {
        requestmessage.reply(resp, options);
      } else {
        requestmessage.reply(resp);
      }
    } else if (resp instanceof byte[]) {
      if (options != null) {
        requestmessage.reply(Buffer.buffer((byte[]) resp), options);
      } else {
        requestmessage.reply(Buffer.buffer((byte[]) resp));
      }
    } else if (resp instanceof JsonObject) {
      if (options != null) {
        requestmessage.reply(JsonObject.class.cast(resp).encode(), options);
      } else {
        requestmessage.reply(JsonObject.class.cast(resp).encode());
      }
    } else if (resp instanceof JsonArray) {
      if (options != null) {
        requestmessage.reply(JsonArray.class.cast(resp).encode(), options);
      } else {
        requestmessage.reply(JsonArray.class.cast(resp).encode());
      }
    }
  }

  /**
   * perform blocking task execution
   *
   * @return the blockingexecution chain {@link org.jacpfx.vxms.event.eventbus.blocking.EventbusBridgeRequest}
   */
  public org.jacpfx.vxms.event.eventbus.blocking.EventbusBridgeRequest blocking() {
    return new org.jacpfx.vxms.event.eventbus.blocking.EventbusBridgeRequest(
        methodId, requestmessage, vxmsShared, failure, errorMethodHandler);
  }
}
