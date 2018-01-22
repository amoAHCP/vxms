/*
 * Copyright [2018] [Andy Moncsek]
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

import static java.util.Optional.ofNullable;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.rest.interfaces.blocking.ExecuteEventbusByteCall;

/**
 * Created by Andy Moncsek on 12.01.16. This class is the end of the blocking fluent API, all data
 * collected to execute the chain.
 */
public class ExecuteRSByte extends org.jacpfx.vxms.rest.response.basic.ExecuteRSByte {

  protected final long delay;
  protected final ExecuteEventbusByteCall excecuteAsyncEventBusAndReply;
  protected final ThrowableSupplier<byte[]> byteSupplier;
  protected final ThrowableFunction<Throwable, byte[]> onFailureRespond;
  protected final List<BlockingExecutionStep> chain;

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
   * @param byteSupplier the supplier, producing the byte response
   * @param chain the execution chain
   * @param excecuteAsyncEventBusAndReply the response of an event-bus call which is passed to the
   *     fluent API
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
  public ExecuteRSByte(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableSupplier<byte[]> byteSupplier,
      List<BlockingExecutionStep> chain,
      ExecuteEventbusByteCall excecuteAsyncEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, byte[]> onFailureRespond,
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
        null,
        null,
        null,
        encoder,
        errorHandler,
        null,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
    this.delay = delay;
    this.excecuteAsyncEventBusAndReply = excecuteAsyncEventBusAndReply;
    this.byteSupplier = byteSupplier;
    this.onFailureRespond = onFailureRespond;
    this.chain = chain;
  }

  @Override
  public void execute(HttpResponseStatus status) {
    Objects.requireNonNull(status);
    new ExecuteRSByte(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            headers,
            byteSupplier,
            chain,
            excecuteAsyncEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            status.code(),
            httpErrorCode,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout)
        .execute();
  }

  @Override
  public void execute(HttpResponseStatus status, String contentType) {
    Objects.requireNonNull(status);
    Objects.requireNonNull(contentType);
    new ExecuteRSByte(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            updateContentType(headers, contentType),
            byteSupplier,
            chain,
            excecuteAsyncEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            status.code(),
            httpErrorCode,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout)
        .execute();
  }

  @Override
  public void execute(String contentType) {
    Objects.requireNonNull(contentType);
    new ExecuteRSByte(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            updateContentType(headers, contentType),
            byteSupplier,
            chain,
            excecuteAsyncEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            httpStatusCode,
            httpErrorCode,
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
                    vxmsShared,
                    failure,
                    errorMethodHandler,
                    context,
                    headers,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
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
                          vxmsShared,
                          failure,
                          errorMethodHandler,
                          errorHandler,
                          supplier,
                          retry,
                          timeout,
                          circuitBreakerTimeout,
                          delay,
                          handler,
                          onFailureRespond),
                  false,
                  getResultHandler(retry, httpErrorCode));
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
                                      vxmsShared,
                                      failure,
                                      errorMethodHandler,
                                      errorHandler,
                                      initialConsumer,
                                      retry,
                                      timeout,
                                      circuitBreakerTimeout,
                                      delay,
                                      handler,
                                      onFailureRespond),
                              false,
                              getResultHandler(
                                  methodId,
                                  vxmsShared,
                                  failure,
                                  errorMethodHandler,
                                  errorHandler,
                                  executionStep,
                                  chainList,
                                  httpErrorCode,
                                  retry,
                                  timeout,
                                  circuitBreakerTimeout,
                                  delay,
                                  onFailureRespond));
                        });
              }
            });
  }
}
