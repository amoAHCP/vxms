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

package org.jacpfx.vxms.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.rest.response.basic.ExecuteRSByteResponse;
import org.jacpfx.vxms.rest.response.basic.ExecuteRSChainResponse;
import org.jacpfx.vxms.rest.response.basic.ExecuteRSObjectResponse;
import org.jacpfx.vxms.rest.response.basic.ExecuteRSStringResponse;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API to define a Task and to reply the request with
 * the output of your task.
 */
public class RESTResponse {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final RoutingContext context;
  private final Map<String, String> headers;

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param context the vertx routing context
   * @param headers the headers to pass to the response
   */
  public RESTResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
    this.headers = headers;
  }

  /**
   * Switch to blocking mode
   *
   * @return {@link RESTResponseBlocking}
   */
  public RESTResponseBlocking blocking() {
    return new RESTResponseBlocking(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers);
  }

  /**
   * Returns a byte array to the target type
   *
   * @param byteConsumer consumes a io.vertx.core.Future to complete with a byte response
   * @return {@link ExecuteRSByteResponse}
   */
  public ExecuteRSByteResponse byteResponse(ThrowableFutureConsumer<byte[]> byteConsumer) {
    return new ExecuteRSByteResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, byteConsumer, null);
  }

  /**
   * Returns a String to the target type
   *
   * @param stringConsumer consumes a io.vertx.core.Future to complete with a String response
   * @return {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse stringResponse(ThrowableFutureConsumer<String> stringConsumer) {
    return new ExecuteRSStringResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, stringConsumer, null);
  }

  /**
   * starts a supply chain to create a response
   *
   * @param chainconsumer the initial supplier
   * @param <T> the type of the return value
   * @return {@link ExecuteRSChainResponse}
   */
  @SuppressWarnings("unchecked")
  public <T> ExecuteRSChainResponse<T> supply(ThrowableFutureConsumer<T> chainconsumer) {
    final List<ExecutionStep> chain = new ArrayList<>();
    chain.add(new ExecutionStep(chainconsumer));
    return new ExecuteRSChainResponse<>(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, chain);
  }

  /**
   * Returns a Serializable to the target type
   *
   * @param objectConsumer consumes a io.vertx.core.Future to complete with a Serialized Object
   *     response
   * @param encoder the encoder to serialize the object response
   * @return {@link ExecuteRSObjectResponse}
   */
  public ExecuteRSObjectResponse objectResponse(
      ThrowableFutureConsumer<Serializable> objectConsumer, Encoder encoder) {
    return new ExecuteRSObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        objectConsumer,
        null,
        encoder);
  }

  /**
   * Ends the createResponse. If no data has been written to the createResponse body, the actual
   * createResponse won'failure get written until this method gets called.
   *
   * <p>Once the createResponse has ended, it cannot be used any more.
   */
  public void end() {
    context.response().end();
  }

  /**
   * Ends the createResponse. If no data has been written to the createResponse body, the actual
   * createResponse won'failure get written until this method gets called.
   *
   * <p>Once the createResponse has ended, it cannot be used any more.
   *
   * @param status, the HTTP Status code
   */
  public void end(HttpResponseStatus status) {
    if (status != null) {
      context.response().setStatusCode(status.code()).end();
    } else {
      context.response().end();
    }
  }
}
