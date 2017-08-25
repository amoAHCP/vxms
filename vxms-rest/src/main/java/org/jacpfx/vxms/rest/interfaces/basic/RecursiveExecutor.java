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

package org.jacpfx.vxms.rest.interfaces.basic;

import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;

/**
 * Created by amo on 31.01.17.
 * Generic Functional interface for handling typed execution of fluid API
 */
@FunctionalInterface
public interface RecursiveExecutor<T> {

  /**
   * Execute typed execution handling
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure the failure thrown while task execution or messaging
   * @param errorMethodHandler the error-method handler
   * @param context the vertx routing context
   * @param headers the headers to pass to the response
   * @param consumer the consumer to finish the response creation
   * @param excecuteEventBusAndReply create the reply
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  void execute(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableFutureConsumer<T> consumer,
      ExecuteEventbusStringCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      int httpStatusCode, int httpErrorCode,
      int retryCount, long timeout, long circuitBreakerTimeout);
}