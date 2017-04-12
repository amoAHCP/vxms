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

package org.jacpfx.vertx.event.response.basic;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.function.Consumer;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusByteCall;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class is the end of the non blocking fluent API, all data collected to execute the chain.
 */
public class ExecuteEventbusBasicByteResponse extends ExecuteEventbusBasicByte {


  public ExecuteEventbusBasicByteResponse(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      ThrowableFutureConsumer<byte[]> byteConsumer,
      ExecuteEventbusByteCall excecuteEventBusAndReply,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    super(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        byteConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * defines an action for errors in byte responses, you can handle the error and return an
   * alternate createResponse value
   *
   * @param onFailureRespond the handler (function) to execute on error
   * @return the createResponse chain {@link ExecuteEventbusBasicByteResponse}
   */
  public ExecuteEventbusBasicByteResponse onFailureRespond(
      ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond) {
    return new ExecuteEventbusBasicByteResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        byteConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * This is an intermediate error method, the error will be passed along the chain (onFailurePass
   * or simply an error)
   *
   * @param errorHandler , a consumer that holds the error
   * @return the response chain {@link ExecuteEventbusBasicByteResponse}
   */
  public ExecuteEventbusBasicByteResponse onError(Consumer<Throwable> errorHandler) {
    return new ExecuteEventbusBasicByteResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        byteConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * Defines how long a method can be executed before aborted.
   *
   * @param timeout the amount of timeout in ms
   * @return the response chain {@link ExecuteEventbusBasicByteResponse}
   */
  public ExecuteEventbusBasicByteResponse timeout(long timeout) {
    return new ExecuteEventbusBasicByteResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        byteConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * retry execution N times before
   *
   * @param retryCount the amount of retries
   * @return the response chain {@link ExecuteEventbusBasicByteCircuitBreaker}
   */
  public ExecuteEventbusBasicByteCircuitBreaker retry(int retryCount) {
    return new ExecuteEventbusBasicByteCircuitBreaker(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        byteConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

}
