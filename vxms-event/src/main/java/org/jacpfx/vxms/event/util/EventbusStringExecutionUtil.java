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

package org.jacpfx.vxms.event.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.event.eventbus.basic.EventbusBridgeExecution;
import org.jacpfx.vxms.event.interfaces.basic.ExecuteEventbusStringCall;
import org.jacpfx.vxms.event.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vxms.event.interfaces.basic.RetryExecutor;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusBasicByteResponse;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusBasicStringResponse;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Typed execution of event-bus calls and string response
 */
public class EventbusStringExecutionUtil {


  /**
   * create execution chain for event-bus request and reply to request event
   *
   * @param _methodId the method identifier
   * @param _targetId the event-bus target id
   * @param _message the message to send
   * @param _stringFunction the function to process the result message
   * @param _requestOptions the event-bus (request) delivery serverOptions
   * @param _vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param _failure the failure thrown while task execution
   * @param _errorMethodHandler the error-method handler
   * @param _requestMessage the request message to respond to
   * @param _stringConsumer the consumer that takes a Future to complete, producing the string
   * response
   * @param _errorHandler the error handler
   * @param _onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param _responseDeliveryOptions the event-bus (response) delivery serverOptions
   * @param _retryCount the amount of retries before failure execution is triggered
   * @param _timeout the amount of time before the execution will be aborted
   * @param _circuitBreakerTimeout the amount of time before the circuit breaker closed again
   * @return the execution chain {@link ExecuteEventbusBasicStringResponse}
   */
  public static ExecuteEventbusBasicStringResponse mapToStringResponse(String _methodId,
      String _targetId,
      Object _message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> _stringFunction,
      DeliveryOptions _requestOptions,
      VxmsShared _vxmsShared,
      Throwable _failure,
      Consumer<Throwable> _errorMethodHandler,
      Message<Object> _requestMessage,
      ThrowableFutureConsumer<String> _stringConsumer,
      Consumer<Throwable> _errorHandler,
      ThrowableErrorConsumer<Throwable, String> _onFailureRespond,
      DeliveryOptions _responseDeliveryOptions,
      int _retryCount,
      long _timeout,
      long _circuitBreakerTimeout) {

    final DeliveryOptions _deliveryOptions = Optional.ofNullable(_requestOptions)
        .orElse(new DeliveryOptions());
    final RecursiveExecutor executor = (methodId,
        vxmsShared,
        t,
        errorMethodHandler,
        requestMessage,
        consumer,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout) ->
        new ExecuteEventbusBasicStringResponse(methodId,
            vxmsShared, t,
            errorMethodHandler,
            requestMessage,
            null,
            consumer,
            null,
            errorHandler,
            onFailureRespond,
            responseDeliveryOptions,
            retryCount,
            timeout,
            circuitBreakerTimeout).
            execute();

    final RetryExecutor retry = (targetId,
        message,
        function,
        requestOptions,
        methodId,
        vxmsShared,
        t,
        errorMethodHandler,
        requestMessage,
        consumer,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout) ->
        mapToStringResponse(methodId, targetId,
            message,
            function,
            requestOptions,
            vxmsShared, t,
            errorMethodHandler,
            requestMessage,
            null,
            errorHandler,
            onFailureRespond,
            responseDeliveryOptions,
            retryCount - 1,
            timeout,
            circuitBreakerTimeout).
            execute();

    final ExecuteEventbusStringCall excecuteEventBusAndReply =
        (methodId,
            vertx,
            errorMethodHandler,
            requestMessage,
            errorHandler,
            onFailureRespond,
            responseDeliveryOptions, retryCount,
            timeout, circuitBreakerTimeout) ->
            EventbusBridgeExecution.sendMessageAndSupplyHandler(methodId, _targetId,
                _message,
                _stringFunction,
                _deliveryOptions,
                vertx,
                errorMethodHandler,
                requestMessage,
                null,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout, executor, retry);

    return new ExecuteEventbusBasicStringResponse(_methodId,
        _vxmsShared,
        _failure,
        _errorMethodHandler,
        _requestMessage,
        null,
        _stringConsumer,
        excecuteEventBusAndReply,
        _errorHandler,
        _onFailureRespond, _responseDeliveryOptions, _retryCount, _timeout, _circuitBreakerTimeout);
  }


}
