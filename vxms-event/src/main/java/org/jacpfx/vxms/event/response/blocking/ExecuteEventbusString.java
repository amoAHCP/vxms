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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.event.interfaces.blocking.ExecuteEventbusStringCallBlocking;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusBasicString;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class is the end of the blocking fluent API, all data collected to execute the chain.
 */
public class ExecuteEventbusString extends ExecuteEventbusBasicString {

  protected final long delay;
  protected final ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply;
  protected final ThrowableSupplier<String> stringSupplier;
  protected final ThrowableFunction<Throwable, String> onFailureRespond;

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param message the message to respond to
   * @param stringSupplier the supplier, producing the byte response
   * @param excecuteAsyncEventBusAndReply handles the response execution after event-bus bridge
   * reply
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param deliveryOptions the response deliver serverOptions
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteEventbusString(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      ThrowableSupplier<String> stringSupplier,
      ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, String> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout) {
    super(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        null,
        null,
        errorHandler,
        null,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
    this.delay = delay;
    this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
    this.stringSupplier = stringSupplier;
    this.onFailureRespond = onFailureRespond;
  }

  @Override
  public void execute(DeliveryOptions deliveryOptions) {
    Objects.requireNonNull(deliveryOptions);
    new ExecuteEventbusString(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        stringSupplier,
        excecuteAsyncEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout).execute();
  }


  @Override
  public void execute() {
    Optional.ofNullable(excecuteAsyncEventBusAndReply).ifPresent(evFunction -> {
      try {
        evFunction.execute(methodId,
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
    Optional.ofNullable(stringSupplier).
        ifPresent(supplier -> {
              int retry = retryCount;
              final Vertx vertx = vxmsShared.getVertx();
              vertx.executeBlocking(handler -> executeAsync(supplier, retry, handler), false,
                  getAsyncResultHandler(retry));
            }

        );
  }

  private void executeAsync(ThrowableSupplier<String> supplier, int retry,
      Future<ExecutionResult<String>> blockingHandler) {
    ResponseBlockingExecution.createResponseBlocking(methodId,
        supplier,
        blockingHandler,
        errorHandler,
        onFailureRespond,
        errorMethodHandler,
        vxmsShared,
        failure,
        retry,
        timeout,
        circuitBreakerTimeout,
        delay);
  }

  private Handler<AsyncResult<ExecutionResult<String>>> getAsyncResultHandler(int retry) {
    return value -> {
      if (!value.failed()) {
        ExecutionResult<String> result = value.result();
        respond(result.getResult());
      } else {
        if (retry == 0) {
          fail(value.cause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
      }
    };
  }

}
