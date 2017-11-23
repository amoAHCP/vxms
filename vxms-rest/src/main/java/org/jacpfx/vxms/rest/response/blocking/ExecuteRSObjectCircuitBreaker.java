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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.rest.interfaces.blocking.ExecuteEventbusObjectCall;

/**
 * Created by Andy Moncsek on 12.01.16. This class defines the fluid API part to define the amount
 * of time after the circuit breaker will be closed again
 */
public class ExecuteRSObjectCircuitBreaker extends ExecuteRSObjectResponse {

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
   * @param objectSupplier the supplier, producing the object response
   * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to
   *     the fluent API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSObjectCircuitBreaker(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableSupplier<Serializable> objectSupplier,
      List<BlockingExecutionStep> chain,
      ExecuteEventbusObjectCall excecuteBlockingEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, Serializable> onFailureRespond,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        objectSupplier,
        chain,
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
   * Defines how long a method can be executed before aborted.
   *
   * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow
   *     "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs
   *     faster)
   * @return the response chain {@link ExecuteRSObjectResponse}
   */
  public ExecuteRSObjectResponse closeCircuitBreaker(long circuitBreakerTimeout) {
    return new ExecuteRSObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        objectSupplier,
        chain,
        excecuteEventBusAndReply,
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
