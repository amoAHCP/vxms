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

package org.jacpfx.vxms.rest.response.basic;

import static java.util.Optional.ofNullable;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.rest.interfaces.basic.ExecuteEventbusByteCall;
import org.jacpfx.vxms.rest.response.AbstractResponse;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class is the end of the fluent API, all data collected to execute the chain.
 */
public class ExecuteRSByte extends AbstractResponse<byte[]> {

  protected final String methodId;
  protected final VxmsShared vxmsShared;
  protected final Throwable failure;
  protected final RoutingContext context;
  protected final Map<String, String> headers;
  protected final Consumer<Throwable> errorHandler;
  protected final Consumer<Throwable> errorMethodHandler;
  protected final ThrowableFutureConsumer<byte[]> byteConsumer;
  protected final List<ExecutionStep> chain;
  protected final ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond;
  protected final ExecuteEventbusByteCall excecuteEventBusAndReply;
  protected final Encoder encoder;
  protected final int httpStatusCode;
  protected final int httpErrorCode;
  protected final int retryCount;
  protected final long timeout;
  protected final long circuitBreakerTimeout;

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
   * @param chain the list of execution steps
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
  public ExecuteRSByte(String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableFutureConsumer<byte[]> byteConsumer,
      List<ExecutionStep> chain,
      ExecuteEventbusByteCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.context = context;
    this.headers = headers;
    this.byteConsumer = byteConsumer;
    this.chain = chain;
    this.encoder = encoder;
    this.errorHandler = errorHandler;
    this.onFailureRespond = onFailureRespond;
    this.retryCount = retryCount;
    this.httpStatusCode = httpStatusCode;
    this.httpErrorCode = httpErrorCode;
    this.excecuteEventBusAndReply = excecuteEventBusAndReply;
    this.timeout = timeout;
    this.circuitBreakerTimeout = circuitBreakerTimeout;
  }

  /**
   * Execute the reply chain with given http status code
   *
   * @param status, the http status code
   */
  public void execute(HttpResponseStatus status) {
    Objects.requireNonNull(status);
    new ExecuteRSByte(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        byteConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        status.code(),
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout).
        execute();
  }

  /**
   * Execute the reply chain with given http status code and content-type
   *
   * @param status, the http status code
   * @param contentType , the html content-type
   */
  public void execute(HttpResponseStatus status, String contentType) {
    Objects.requireNonNull(status);
    Objects.requireNonNull(contentType);
    new ExecuteRSByte(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        updateContentType(headers, contentType),
        byteConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        status.code(),
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout).
        execute();
  }

  /**
   * Executes the reply chain with given html content-type
   *
   * @param contentType, the html content-type
   */
  public void execute(String contentType) {
    Objects.requireNonNull(contentType);
    new ExecuteRSByte(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        updateContentType(headers, contentType),
        byteConsumer,
        chain,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout).
        execute();
  }


  /**
   * Execute the reply chain
   */
  public void execute() {
    final Vertx vertx = vxmsShared.getVertx();
    vertx.runOnContext(action -> {
      ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
        try {
          evFunction.execute(vxmsShared,
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
              circuitBreakerTimeout);
        } catch (Exception e) {
          e.printStackTrace();
        }

      });

      ofNullable(byteConsumer).
          ifPresent(userOperation -> {
                int retry = retryCount;
                ResponseExecution.createResponse(methodId,
                    userOperation,
                    errorHandler,
                    onFailureRespond,
                    errorMethodHandler,
                    vxmsShared,
                    failure,
                    value -> {
                      if (value.succeeded()) {
                        if (!value.handledError()) {
                          respond(value.getResult());
                        } else {
                          respond(value.getResult(), httpErrorCode);
                        }

                      } else {
                        errorRespond(value.getCause().getMessage(),
                            HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                      }
                      checkAndCloseResponse(retry);
                    }, retry, timeout, circuitBreakerTimeout);
              }
          );


      ofNullable(chain).ifPresent(chainList -> {
        if (!chainList.isEmpty()) {
          final ExecutionStep executionStep = chainList.get(0);
          ofNullable(executionStep.getChainconsumer()).ifPresent(initialConsumer -> {
            int retry = retryCount;
            ResponseExecution.createResponse(methodId,
                initialConsumer,
                errorHandler,
                onFailureRespond,
                errorMethodHandler,
                vxmsShared,
                failure,
                (value) -> {
                  if (value.succeeded()) {
                    if (!value.handledError()) {
                      final Object result = value.getResult();
                      if (chainList.size() > 1) {
                        final ExecutionStep executionStepAndThan = chainList.get(1);
                        ofNullable(executionStepAndThan.getStep()).ifPresent(step ->
                            executeStep(
                                methodId,
                                vxmsShared,
                                failure,
                                errorMethodHandler,
                                errorHandler,
                                onFailureRespond,
                                chainList,
                                retry,
                                httpErrorCode,
                                timeout,
                                circuitBreakerTimeout,
                                result,
                                executionStepAndThan,
                                step));
                      }else {
                        respond(value.getResult());
                      }
                    } else {
                      // handle on failure response
                      respond(value.getResult(), httpErrorCode);
                    }

                  } else {
                    // reply unhandled error
                    errorRespond(value.getCause().getMessage(),
                        HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                  }
                  checkAndCloseResponse(retry);
                }, retry, timeout, circuitBreakerTimeout);
          });
        }
      });

    });

  }


  @Override
  protected void checkAndCloseResponse(int retry) {
    final HttpServerResponse response = context.response();
    if (retry == 0 && !response.ended()) {
      response.end();
    }
  }
  @Override
  protected void respond(byte[] result, int statuscode) {
    final HttpServerResponse response = context.response();
    if (!response.ended()) {
      ResponseExecution.updateHeaderAndStatuscode(headers, statuscode, response);
      if (result != null) {
        response.end(Buffer.buffer(result));
      } else {
        response.end();
      }
    }
  }

  @Override
  protected void errorRespond(String result, int statuscode) {
    final HttpServerResponse response = context.response();
    if (!response.ended()) {
      ResponseExecution.updateHeaderAndStatuscode(headers, statuscode, response);
      if (result != null) {
        response.end(result);
      } else {
        response.end();
      }
    }
  }
  @Override
  protected void respond(byte[] result) {
    respond(result, httpStatusCode);
  }


}
