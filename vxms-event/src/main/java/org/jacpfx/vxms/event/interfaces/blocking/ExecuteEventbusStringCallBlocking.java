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

package org.jacpfx.vxms.event.interfaces.blocking;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;

/**
 * Created by Andy Moncsek on 21.03.16. Typed functional interface called on event-bus response. The
 * execution will be handled as blocking code.
 */
@FunctionalInterface
public interface ExecuteEventbusStringCallBlocking {

  /**
   * Execute  chain when event-bus response handler is executed
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param errorMethodHandler the error-method handler
   * @param requestMessage the message to reply
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param responseDeliveryOptions the delivery options for the response
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the delay time in ms between an execution error and the retry
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  void execute(String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, String> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount, long timeout, long delay, long circuitBreakerTimeout);
}
