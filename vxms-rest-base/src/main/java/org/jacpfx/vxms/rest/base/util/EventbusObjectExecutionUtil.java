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

import org.jacpfx.vxms.rest.base.interfaces.basic.ExecuteEventbusObjectCall;
import org.jacpfx.vxms.rest.base.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vxms.rest.base.interfaces.basic.RetryExecutor;
import org.jacpfx.vxms.rest.base.response.basic.ExecuteRSObjectResponse;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.rest.base.eventbus.basic.EventbusExecution;

/** Created by Andy Moncsek on 05.04.16. Typed execution of event-bus calls and string response */
public class EventbusObjectExecutionUtil {

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
   * @param _encoder the encoder to encode your objects
   * @return the execution chain {@link ExecuteRSObjectResponse}
   */
  public static ExecuteRSObjectResponse mapToObjectResponse(
      String _methodId,
      String _targetId,
      Object _message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> _objectFunction,
      DeliveryOptions _options,
      VxmsShared _vxmsShared,
      Throwable _failure,
      Consumer<Throwable> _errorMethodHandler,
      RoutingContext _context,
      Encoder _encoder) {

    return mapToObjectResponse(
        _methodId,
        _targetId,
        _message,
        _objectFunction,
        _options,
        _vxmsShared,
        _failure,
        _errorMethodHandler,
        _context,
        null,
        null,
        _encoder,
        null,
        null,
        0,
        0,
        0,
        0,
        0);
  }

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
   * @param _objectConsumer the consumer that takes a Future to complete, producing the string
   *     response
   * @param _encoder the encoder to encode your objects
   * @param _errorHandler the error handler
   * @param _onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param _httpStatusCode the http status code to set for response
   * @param _httpErrorCode the http error code to set in case of failure handling
   * @param _retryCount the amount of retries before failure execution is triggered
   * @param _timeout the amount of time before the execution will be aborted
   * @param _circuitBreakerTimeout the amount of time before the circuit breaker closed again
   * @return the execution chain {@link ExecuteRSObjectResponse}
   */
  public static ExecuteRSObjectResponse mapToObjectResponse(
      String _methodId,
      String _targetId,
      Object _message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> _objectFunction,
      DeliveryOptions _options,
      VxmsShared _vxmsShared,
      Throwable _failure,
      Consumer<Throwable> _errorMethodHandler,
      RoutingContext _context,
      Map<String, String> _headers,
      ThrowableFutureConsumer<Serializable> _objectConsumer,
      Encoder _encoder,
      Consumer<Throwable> _errorHandler,
      ThrowableErrorConsumer<Throwable, Serializable> _onFailureRespond,
      int _httpStatusCode,
      int _httpErrorCode,
      int _retryCount,
      long _timeout,
      long _circuitBreakerTimeout) {

    final DeliveryOptions _deliveryOptions =
        Optional.ofNullable(_options).orElse(new DeliveryOptions());

    final RetryExecutor<Serializable> retry =
        (methodId,
            id,
            message,
            stringFunction,
            deliveryOptions,
            vxmsShared,
            t,
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
            circuitBreakerTimeout) -> {
          final int decrementedCount = retryCount - 1;

          mapToObjectResponse(
                  methodId,
                  id,
                  message,
                  stringFunction,
                  deliveryOptions,
                  vxmsShared,
                  t,
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
                  circuitBreakerTimeout)
              .execute();
        };
    final RecursiveExecutor<Serializable> executor =
        (methodId,
            vxmsShared,
            t,
            errorMethodHandler,
            context,
            headers,
            stringConsumer,
            excecuteEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            httpStatusCode,
            httpErrorCode,
            retryCount,
            timeout,
            circuitBreakerTimeout) ->
            new ExecuteRSObjectResponse(
                    methodId,
                    vxmsShared,
                    t,
                    errorMethodHandler,
                    context,
                    headers,
                    stringConsumer,
                    null,
                    null,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout,
                    circuitBreakerTimeout)
                .execute();

    final ExecuteEventbusObjectCall excecuteEventBusAndReply =
        (vxmsShared,
            t,
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
            circuitBreakerTimeout) ->
            EventbusExecution.sendMessageAndSupplyHandler(
                _methodId,
                _targetId,
                _message,
                _objectFunction,
                _deliveryOptions,
                vxmsShared,
                t,
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
        _objectConsumer,
        null,
        excecuteEventBusAndReply,
        _encoder,
        _errorHandler,
        _onFailureRespond,
        _httpStatusCode,
        _httpErrorCode,
        _retryCount,
        _timeout,
        _circuitBreakerTimeout);
  }
}
