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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.rest.base.response.blocking.ExecuteRSByteResponse;
import org.jacpfx.vxms.rest.base.response.blocking.ExecuteRSChainResponse;
import org.jacpfx.vxms.rest.base.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vxms.rest.base.response.blocking.ExecuteRSStringResponse;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API to define a Task and to reply the request with
 * the output of your task.
 */
public class RESTResponseBlocking {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final RoutingContext context;
  private final Map<String, String> headers;

  /**
   * Pass all needed values to execute the chain
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param context the vertx routing context
   * @param headers the headers to pass to the response
   */
  public RESTResponseBlocking(
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
   * Retunrs a byte array to the target type
   *
   * @param byteSupplier supplier which returns the createResponse value as byte array
   * @return {@link ExecuteRSByteResponse}
   */
  public ExecuteRSByteResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
    return new ExecuteRSByteResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, byteSupplier, null);
  }

  /**
   * Retunrs a String to the target type
   *
   * @param stringSupplier supplier which returns the createResponse value as String
   * @return {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
    return new ExecuteRSStringResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, stringSupplier, null);
  }

  /**
   * Retunrs a Serializable to the target type
   *
   * @param objectSupplier supplier which returns the createResponse value as Serializable
   * @param encoder the encoder to serialize the object response
   * @return {@link ExecuteRSObjectResponse}
   */
  public ExecuteRSObjectResponse objectResponse(
      ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
    return new ExecuteRSObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        objectSupplier,
        null,
        encoder);
  }

  /**
   * starts a supply chain to create a blocking response
   *
   * @param chainsupplier the first step in the execution chain
   * @param <T> the reutrn type of the supplier
   * @return {@link ExecuteRSChainResponse}
   */
  @SuppressWarnings("unchecked")
  public <T> ExecuteRSChainResponse<T> supply(ThrowableSupplier<T> chainsupplier) {
    final List<BlockingExecutionStep> chain = new ArrayList<>();
    chain.add(new BlockingExecutionStep(chainsupplier));
    return new ExecuteRSChainResponse<>(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, chain);
  }
}
