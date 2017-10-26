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

package org.jacpfx.vxms.rest.response.blocking;

import io.vertx.ext.web.RoutingContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.rest.interfaces.basic.ExecuteEventbusStringCall;

public class ExecuteRSChainResponse<T> {
  protected final String methodId;
  protected final VxmsShared vxmsShared;
  protected final Throwable failure;
  protected final Consumer<Throwable> errorMethodHandler;
  protected final RoutingContext context;
  protected final Map<String, String> headers;
  protected final List<BlockingExecutionStep> chain;
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
   * objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param context the vertx routing context
   * @param headers the headers to pass to the response
   * @param chain the list of execution steps
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   * API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSChainResponse(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      List<BlockingExecutionStep> chain,
      ExecuteEventbusStringCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      int httpStatusCode, int httpErrorCode,
      int retryCount, long timeout, long circuitBreakerTimeout) {
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

  public ExecuteRSChainResponse(String methodId, VxmsShared vxmsShared, Throwable failure,
      Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
      List<BlockingExecutionStep> chain) {
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

  public <H> ExecuteRSChainResponse<H> andThen(ThrowableFunction<T,H> step) {
    final List<BlockingExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new BlockingExecutionStep(step));
    return new ExecuteRSChainResponse<>(methodId,
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
   * Retunrs a String to the target type
   *
   * @param step supplier which returns the createResponse value as String
   * @return {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse mapToStringResponse(ThrowableFunction<T,String> step) {
    final List<BlockingExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new BlockingExecutionStep(step));
    return new ExecuteRSStringResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        null,
        chainTmp
    );
  }

  /**
   * Retunrs a byte array to the target type
   *
   * @param step supplier which returns the createResponse value as byte array
   * @return {@link ExecuteRSByteResponse}
   */
  public ExecuteRSByteResponse mapToByteResponse(ThrowableFunction<T,byte[]> step) {
    final List<BlockingExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new BlockingExecutionStep(step));
    return new ExecuteRSByteResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        null,
        chainTmp);
  }

  /**
   * Retunrs a Serializable to the target type
   *
   * @param step supplier which returns the createResponse value as Serializable
   * @param encoder the encoder to serialize the object response
   * @return {@link ExecuteRSObjectResponse}
   */
  public ExecuteRSObjectResponse mapToObjectResponse(ThrowableFunction<T,Serializable> step,
      Encoder encoder) {
    final List<BlockingExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new BlockingExecutionStep(step));
    return new ExecuteRSObjectResponse(methodId,
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
