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

package org.jacpfx.vxms.rest.response.basic;

import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.rest.interfaces.basic.ExecuteEventbusStringCall;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API for byte responses, defines access to failure
 * handling, timeouts,...
 */
public class ExecuteRSStringResponse extends ExecuteRSString {

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
   * @param stringConsumer the consumer that takes a Future to complete, producing the string
   *     response
   * @param chain the execution steps when using *supply/andThen*
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   *     API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSStringResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableFutureConsumer<String> stringConsumer,
      List<ExecutionStep> chain,
      ExecuteEventbusStringCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, String> onFailureRespond,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  public ExecuteRSStringResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableFutureConsumer<String> stringConsumer,
      List<ExecutionStep> chain) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringConsumer,
        chain,
        null,
        null,
        null,
        null,
        0,
        0,
        0,
        0L,
        0L);
  }

  /**
   * intermediate error handler which will be called on each error (at least 1 time, in case on N
   * retries... up to N times)
   *
   * @param errorHandler the handler to be executed on each error
   * @return the response chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse onError(Consumer<Throwable> errorHandler) {
    return new ExecuteRSStringResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * Defines how long a method can be executed before aborted.
   *
   * @param timeout the amount of timeout in ms
   * @return the response chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse timeout(long timeout) {
    return new ExecuteRSStringResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * retry execution N times before
   *
   * @param retryCount the amount of retries
   * @return the response chain {@link ExecuteRSStringCircuitBreaker}
   */
  public ExecuteRSStringCircuitBreaker retry(int retryCount) {
    return new ExecuteRSStringCircuitBreaker(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * defines an action for errors in byte responses, you can handle the error and return an
   * alternate createResponse value, this handler is a terminal handler and will be executed only
   * once
   *
   * @param onFailureRespond the handler (function) to execute on error
   * @return the response chain {@link ExecuteRSStringOnFailureCode}
   */
  public ExecuteRSStringOnFailureCode onFailureRespond(
      ThrowableErrorConsumer<Throwable, String> onFailureRespond) {
    return new ExecuteRSStringOnFailureCode(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * put HTTP header to response
   *
   * @param key the header name
   * @param value the header value
   * @return the response chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse putHeader(String key, String value) {
    Map<String, String> headerMap = new HashMap<>(headers);
    headerMap.put(key, value);
    return new ExecuteRSStringResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headerMap,
        stringConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }
}
