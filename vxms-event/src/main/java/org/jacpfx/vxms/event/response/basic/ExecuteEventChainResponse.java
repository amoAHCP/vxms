/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License; Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing; software
 * distributed under the License is distributed on an "AS IS" BASIS;
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.event.response.basic;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.event.interfaces.basic.ExecuteEventbusObjectCall;

public class ExecuteEventChainResponse<T> {
  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final Message<Object> message;
  private final List<ExecutionStep> chain;
  private final ExecuteEventbusObjectCall excecuteEventBusAndReply;
  private final Encoder encoder;
  private final Consumer<Throwable> errorHandler;
  private final ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond;
  private final DeliveryOptions deliveryOptions;
  private final int retryCount;
  private final long timeout;
  private final long circuitBreakerTimeout;

  public ExecuteEventChainResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      List<ExecutionStep> chain,
      ExecuteEventbusObjectCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.message = message;
    this.chain = chain;
    this.excecuteEventBusAndReply = excecuteEventBusAndReply;
    this.encoder = encoder;
    this.errorHandler = errorHandler;
    this.onFailureRespond = onFailureRespond;
    this.deliveryOptions = deliveryOptions;
    this.retryCount = retryCount;
    this.timeout = timeout;
    this.circuitBreakerTimeout = circuitBreakerTimeout;
  }

  public ExecuteEventChainResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      List<ExecutionStep> chain) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.message = message;
    this.chain = chain;
    this.excecuteEventBusAndReply = null;
    this.encoder = null;
    this.errorHandler = null;
    this.onFailureRespond = null;
    this.deliveryOptions = null;
    this.retryCount = 0;
    this.timeout = 0L;
    this.circuitBreakerTimeout = 0L;
  }

  /**
   * add an other step to execution chain
   * @param step the execution step
   * @param <H> the return type of the step
   * @return the chain to perform other steps
   */
  @SuppressWarnings("unchecked")
  public <H> ExecuteEventChainResponse<H> andThen(ThrowableFutureBiConsumer<T, H> step) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteEventChainResponse<>(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chainTmp,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * Returns a byte array to the target type
   *
   * @param step the execution step to map the response to byte[] response
   * @return {@link ExecuteEventbusByteResponse}
   */
  @SuppressWarnings("unchecked")
  public ExecuteEventbusByteResponse mapToByteResponse(
      ThrowableFutureBiConsumer<T, byte[]> step) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteEventbusByteResponse(
        methodId, vxmsShared, failure, errorMethodHandler, message, chainTmp, null);
  }

  /**
   * Returns a String to the target type
   *
   * @param step the execution step to map the response to String response
   * @return {@link ExecuteEventbusStringResponse}
   */
  @SuppressWarnings("unchecked")
  public ExecuteEventbusStringResponse mapToStringResponse(
      ThrowableFutureBiConsumer<T, String> step) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteEventbusStringResponse(
        methodId, vxmsShared, failure, errorMethodHandler, message, chainTmp, null);
  }

  /**
   * Returns a Serializable to the target type
   *
   * @param step the execution step to map the response to Object response
   * @param encoder the encoder to serialize the response object
   * @return {@link ExecuteEventbusObjectResponse}
   */
  @SuppressWarnings("unchecked")
  public ExecuteEventbusObjectResponse mapToObjectResponse(
      ThrowableFutureBiConsumer<T, Serializable> step, Encoder encoder) {
    final List<ExecutionStep> chainTmp = new ArrayList<>(chain);
    chainTmp.add(new ExecutionStep(step));
    return new ExecuteEventbusObjectResponse(
        methodId, vxmsShared, failure, errorMethodHandler, message, chainTmp, null, encoder);
  }
}
