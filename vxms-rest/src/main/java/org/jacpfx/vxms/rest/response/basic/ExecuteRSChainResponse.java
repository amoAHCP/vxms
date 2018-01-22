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

package org.jacpfx.vxms.rest.response.basic;

import io.vertx.ext.web.RoutingContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.rest.interfaces.basic.ExecuteEventbusStringCall;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API for byte responses, defines access to failure
 * handling, timeouts,...
 */
public class ExecuteRSChainResponse<T> {

  protected final String methodId;
  protected final VxmsShared vxmsShared;
  protected final Throwable failure;
  protected final Consumer<Throwable> errorMethodHandler;
  protected final RoutingContext context;
  protected final Map<String, String> headers;
  protected final List<ExecutionStep> chain;
  protected final Encoder encoder;
  protected final Consumer<Throwable> errorHandler;
  protected final ExecuteEventbusStringCall excecuteEventBusAndReply;
  protected final int httpStatusCode;
  protected final int httpErrorCode;
  protected final int retryCount;
  protected final long timeout;
  protected final long circuitBreakerTimeout;

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
   * @param chain the list of execution steps
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   *     API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSChainResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      List<ExecutionStep> chain,
      ExecuteEventbusStringCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
    this.headers = headers;
    this.chain = chain;
    this.excecuteEventBusAndReply = excecuteEventBusAndReply;
    this.encoder = encoder;
    this.errorHandler = errorHandler;
    this.retryCount = retryCount;
    this.httpStatusCode = httpStatusCode;
    this.httpErrorCode = httpErrorCode;
    this.timeout = timeout;
    this.circuitBreakerTimeout = circuitBreakerTimeout;
  }

  public ExecuteRSChainResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      List<ExecutionStep> chain) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
    this.headers = headers;
    this.excecuteEventBusAndReply = null;
    this.encoder = null;
    this.errorHandler = null;
    this.retryCount = 0;
    this.httpStatusCode = 0;
    this.httpErrorCode = 0;
    this.timeout = 0;
    this.circuitBreakerTimeout = 0;
    this.chain = chain;
  }

  /**
   * @param step the step execution function
   * @param <H> the return type of the step
   * @return the chain to perform other steps
   */
  @SuppressWarnings("unchecked")
  public <H> ExecuteRSChainResponse<H> andThen(ThrowableFutureBiConsumer<T, H> step) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteRSChainResponse<>(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        chainTmp,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * Returns a String to the target type
   *
   * @param step the execution step to map the response to string
   * @return {@link ExecuteRSStringResponse}
   */
  @SuppressWarnings("unchecked")
  public ExecuteRSStringResponse mapToStringResponse(ThrowableFutureBiConsumer<T, String> step) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteRSStringResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, null, chainTmp);
  }

  /**
   * Returns a byte array to the target type
   *
   * @param step the execution step to map the response to byte[]
   * @return {@link ExecuteRSByteResponse}
   */
  @SuppressWarnings("unchecked")
  public ExecuteRSByteResponse mapToByteResponse(ThrowableFutureBiConsumer<T, byte[]> step) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteRSByteResponse(
        methodId, vxmsShared, failure, errorMethodHandler, context, headers, null, chainTmp);
  }

  /**
   * Returns a Serializable to the target type
   *
   * @param step the execution step to map the response to Object
   * @param encoder the encoder to serialize the object response
   * @return {@link ExecuteRSObjectResponse}
   */
  @SuppressWarnings("unchecked")
  public ExecuteRSObjectResponse mapToObjectResponse(
      ThrowableFutureBiConsumer<T, Serializable> step, Encoder encoder) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteRSObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        null,
        chainTmp,
        encoder);
  }
}
