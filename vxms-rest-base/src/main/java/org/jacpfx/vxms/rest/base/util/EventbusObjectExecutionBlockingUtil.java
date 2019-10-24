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

package org.jacpfx.vxms.rest.base.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jacpfx.vxms.rest.base.eventbus.blocking.EventbusExecution;
import org.jacpfx.vxms.rest.base.interfaces.blocking.ExecuteEventbusObjectCall;
import org.jacpfx.vxms.rest.base.interfaces.blocking.RecursiveExecutor;
import org.jacpfx.vxms.rest.base.interfaces.blocking.RetryExecutor;
import org.jacpfx.vxms.rest.base.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vxms.rest.base.response.blocking.ExecuteRSStringResponse;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;

/**
 * Created by Andy Moncsek on 05.04.16. Typed execution of event-bus calls and blocking object
 * response
 */
@SuppressWarnings("ALL")
public class EventbusObjectExecutionBlockingUtil {

  /**
   * create execution chain for event-bus request and reply to rest
   *
   * @param _methodId the method identifier
   * @param _targetId the event-bus target id
   * @param _message the message to send
   * @param _objectFunction the function to process the result message
   * @param _options the event-bus delivery serverOptions
   * @param _vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param _failure the failure thrown while task execution
   * @param _errorMethodHandler the error-method handler
   * @param _context the vertx routing context
   * @param _headers the headers to pass to the response
   * @param _objectSupplier the supplier, producing the object response
   * @param _encoder the encoder to encode your objects
   * @param _errorHandler the error handler
   * @param _onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param _httpStatusCode the http status code to set for response
   * @param _httpErrorCode the http error code to set in case of failure handling
   * @param _retryCount the amount of retries before failure execution is triggered
   * @param _timeout the amount of time before the execution will be aborted
   * @param _delay the delay between an error and the retry
   * @param _circuitBreakerTimeout the amount of time before the circuit breaker closed again
   * @return the execution chain {@link ExecuteRSStringResponse}
   */
  public static ExecuteRSObjectResponse mapToObjectResponse(
      String _methodId,
      String _targetId,
      Object _message,
      DeliveryOptions _options,
      ThrowableFunction<AsyncResult<Message<Object>>, Serializable> _objectFunction,
      VxmsShared _vxmsShared,
      Throwable _failure,
      Consumer<Throwable> _errorMethodHandler,
      RoutingContext _context,
      Map<String, String> _headers,
      ThrowableSupplier<Serializable> _objectSupplier,
      Encoder _encoder,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, Serializable> _onFailureRespond,
      int _httpStatusCode,
      int _httpErrorCode,
      int _retryCount,
      long _timeout,
      long _delay,
      long _circuitBreakerTimeout) {
    final DeliveryOptions _deliveryOptions =
        Optional.ofNullable(_options).orElse(new DeliveryOptions());

    final RetryExecutor<Serializable> retry =
        (methodId,
            targetId,
            message,
            byteFunction,
            deliveryOptions,
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
            circuitBreakerTimeout) -> {
          final int decrementedCount = retryCount - 1;
          mapToObjectResponse(
                  methodId,
                  targetId,
                  message,
                  deliveryOptions,
                  byteFunction,
                  vxmsShared,
                  failure,
                  errorMethodHandler,
                  context,
                  headers,
                  null,
                  encoder,
                  errorHandler,
                  onFailureRespond,
                  httpStatusCode,
                  httpErrorCode,
                  decrementedCount,
                  timeout,
                  delay,
                  circuitBreakerTimeout)
              .execute();
        };

    final RecursiveExecutor<Serializable> executor =
        (methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            headers,
            supplier,
            encoder,
            errorHandler,
            onFailureRespond,
            httpStatusCode,
            httpErrorCode,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout) ->
            new ExecuteRSObjectResponse(
                    methodId,
                    vxmsShared,
                    failure,
                    errorMethodHandler,
                    context,
                    headers,
                    supplier,
                    null,
                    null,
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

    final ExecuteEventbusObjectCall excecuteEventBusAndReply =
        (vxmsShared,
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
            circuitBreakerTimeout) ->
            EventbusExecution.sendMessageAndSupplyHandler(
                _methodId,
                _targetId,
                _message,
                _objectFunction,
                _deliveryOptions,
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
                circuitBreakerTimeout,
                executor,
                retry);
    return new ExecuteRSObjectResponse(
        _methodId,
        _vxmsShared,
        _failure,
        _errorMethodHandler,
        _context,
        _headers,
        _objectSupplier,
        null,
        excecuteEventBusAndReply,
        _encoder,
        _errorHandler,
        _onFailureRespond,
        _httpStatusCode,
        _httpErrorCode,
        _retryCount,
        _timeout,
        _delay,
        _circuitBreakerTimeout);
  }
}
