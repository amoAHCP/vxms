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

package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventbusByteCall;

/**
 * Created by Andy Moncsek on 12.01.16. Defines the fluent API to set the http error code in case of
 * the onFailure method is executed
 */
public class ExecuteRSBasicByteOnFailureCode extends ExecuteRSBasicByteResponse {

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
   * @param byteConsumer the consumer that takes a Future to complete, producing the byte response
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   * API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSBasicByteOnFailureCode(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableFutureConsumer<byte[]> byteConsumer,
      ExecuteEventbusByteCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    super(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        byteConsumer,
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
   * Define the HTTP Code in case of onFailure execution
   *
   * @param httpErrorCode the http error code to set for response, in case of error
   * @return the response chain {@link ExecuteRSBasicByteResponse}
   */
  public ExecuteRSBasicByteResponse httpErrorCode(HttpResponseStatus httpErrorCode) {
    return new ExecuteRSBasicByteResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        byteConsumer,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode.code(),
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }


}
