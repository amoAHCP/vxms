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

package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.common.throwable.ThrowableFunction;
import org.jacpfx.common.throwable.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventbusStringCallBlocking;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for byte responses, defines access to failure handling, timeouts,...
 */
public class ExecuteRSStringResponse extends ExecuteRSString {

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
   * @param stringSupplier the supplier, producing the byte response
   * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to
   * the fluent API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSStringResponse(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableSupplier<String> stringSupplier,
      ExecuteEventbusStringCallBlocking excecuteBlockingEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, String> onFailureRespond,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout) {
    super(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringSupplier,
        excecuteBlockingEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }


  /**
   * defines an action for errors in byte responses, you can handle the error and return an
   * alternate createResponse value
   *
   * @param onFailureRespond the handler (function) to execute on error
   * @return the createResponse chain {@link ExecuteRSStringOnFailureCode}
   */
  public ExecuteRSStringOnFailureCode onFailureRespond(
      ThrowableFunction<Throwable, String> onFailureRespond) {
    return new ExecuteRSStringOnFailureCode(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * intermediate error handler which will be called on each error (at least 1 time, in case on N
   * retries... up to N times)
   *
   * @param errorHandler the handler to be executed on each error
   * @return the response chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse onError(Consumer<Throwable> errorHandler) {
    return new ExecuteRSStringResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * retry operation on error
   *
   * @param retryCount the amount of retries before failure
   * @return the createResponse chain {@link ExecuteRSStringCircuitBreaker}
   */
  public ExecuteRSStringCircuitBreaker retry(int retryCount) {
    return new ExecuteRSStringCircuitBreaker(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * Defines how long a method can be executed before aborted.
   *
   * @param timeout time to wait in ms
   * @return the createResponse chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse timeout(long timeout) {
    return new ExecuteRSStringResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * Defines the delay (in ms) between the createResponse retries (on error).
   *
   * @param delay the amount of time in ms between an error and the retry
   * @return the createResponse chain {@link ExecuteRSStringResponse}
   */
  public ExecuteRSStringResponse delay(long delay) {
    return new ExecuteRSStringResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
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
    return new ExecuteRSStringResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headerMap,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

}
