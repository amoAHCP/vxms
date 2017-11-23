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

package org.jacpfx.vxms.event.response.blocking;

import static java.util.Optional.ofNullable;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.event.interfaces.blocking.ExecuteEventbusByteCall;

/**
 * Created by Andy Moncsek on 12.01.16. This class is the end of the blocking fluent API, all data
 * collected to execute the chain.
 */
public class ExecuteEventbusByte extends org.jacpfx.vxms.event.response.basic.ExecuteEventbusByte {

  protected final long delay;
  protected final ExecuteEventbusByteCall excecuteAsyncEventBusAndReply;
  protected final ThrowableSupplier<byte[]> byteSupplier;
  protected final List<BlockingExecutionStep> chain;
  protected final ThrowableFunction<Throwable, byte[]> onFailureRespond;

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param message the message to responde to
   * @param chain the execution chain
   * @param byteSupplier the supplier, producing the byte response
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   *     API
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param deliveryOptions the response delivery serverOptions
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteEventbusByte(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      List<BlockingExecutionStep> chain,
      ThrowableSupplier<byte[]> byteSupplier,
      ExecuteEventbusByteCall excecuteEventBusAndReply,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, byte[]> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        null,
        null,
        null,
        errorHandler,
        null,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
    this.chain = chain;
    this.delay = delay;
    this.excecuteAsyncEventBusAndReply = excecuteEventBusAndReply;
    this.byteSupplier = byteSupplier;
    this.onFailureRespond = onFailureRespond;
  }

  @Override
  public void execute(DeliveryOptions deliveryOptions) {
    Objects.requireNonNull(deliveryOptions);
    new ExecuteEventbusByte(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            message,
            chain,
            byteSupplier,
            excecuteAsyncEventBusAndReply,
            errorHandler,
            onFailureRespond,
            deliveryOptions,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout)
        .execute();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute() {
    Optional.ofNullable(excecuteAsyncEventBusAndReply)
        .ifPresent(
            evFunction -> {
              try {
                evFunction.execute(
                    methodId,
                    vxmsShared,
                    errorMethodHandler,
                    message,
                    errorHandler,
                    onFailureRespond,
                    deliveryOptions,
                    retryCount,
                    timeout,
                    delay,
                    circuitBreakerTimeout);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
    Optional.ofNullable(byteSupplier)
        .ifPresent(
            supplier -> {
              int retry = retryCount;
              final Vertx vertx = vxmsShared.getVertx();
              vertx.executeBlocking(
                  handler ->
                      executeBlocking(
                          methodId,
                          supplier,
                          handler,
                          errorHandler,
                          onFailureRespond,
                          errorMethodHandler,
                          vxmsShared,
                          failure,
                          retry,
                          timeout,
                          circuitBreakerTimeout,
                          delay),
                  false,
                  getBlockingResultHandler(retry));
            });

    ofNullable(chain)
        .ifPresent(
            (List<BlockingExecutionStep> chainList) -> {
              if (!chainList.isEmpty()) {
                final BlockingExecutionStep executionStep = chainList.get(0);
                ofNullable(executionStep.getChainsupplier())
                    .ifPresent(
                        (initialConsumer) -> {
                          int retry = retryCount;
                          final Vertx vertx = vxmsShared.getVertx();
                          vertx.executeBlocking(
                              handler ->
                                  executeBlocking(
                                      methodId,
                                      initialConsumer,
                                      handler,
                                      errorHandler,
                                      onFailureRespond,
                                      errorMethodHandler,
                                      vxmsShared,
                                      failure,
                                      retry,
                                      timeout,
                                      circuitBreakerTimeout,
                                      delay),
                              false,
                              getBlockingResultHandler(
                                  methodId,
                                  executionStep,
                                  chainList,
                                  errorHandler,
                                  onFailureRespond,
                                  errorMethodHandler,
                                  vxmsShared,
                                  failure,
                                  retry,
                                  timeout,
                                  circuitBreakerTimeout,
                                  delay));
                        });
              }
            });
  }
}
