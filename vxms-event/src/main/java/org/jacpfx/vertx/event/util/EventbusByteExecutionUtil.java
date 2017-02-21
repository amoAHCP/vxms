package org.jacpfx.vertx.event.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.eventbus.basic.EventbusExecution;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusByteCall;
import org.jacpfx.vertx.event.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vertx.event.interfaces.basic.RetryExecutor;
import org.jacpfx.vertx.event.response.basic.ExecuteRSBasicByteResponse;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Typed execution of event-bus calls and byte response
 */
public class EventbusByteExecutionUtil {

    /**
     * create execution chain for event-bus request and reply to request event
     *
     * @param _methodId                the method identifier
     * @param _targetId                the event-bus target id
     * @param _message                 the message to send
     * @param _byteFunction            the function to process the result message
     * @param _requestOptions          the event-bus (request) delivery options
     * @param _vertx                   the vertx instance
     * @param _failure                 the failure thrown while task execution
     * @param _errorMethodHandler      the error-method handler
     * @param _requestMessage          the request message to respond to
     * @param _byteConsumer            the consumer that takes a Future to complete, producing the string response
     * @param _errorHandler            the error handler
     * @param _onFailureRespond        the consumer that takes a Future with the alternate response value in case of failure
     * @param _responseDeliveryOptions the event-bus (response) delivery options
     * @param _retryCount              the amount of retries before failure execution is triggered
     * @param _timeout                 the amount of time before the execution will be aborted
     * @param _circuitBreakerTimeout   the amount of time before the circuit breaker closed again
     * @return the execution chain {@link ExecuteRSBasicByteResponse}
     */
    public static ExecuteRSBasicByteResponse mapToByteResponse(String _methodId,
                                                               String _targetId,
                                                               Object _message,
                                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> _byteFunction,
                                                               DeliveryOptions _requestOptions,
                                                               Vertx _vertx,
                                                               Throwable _failure,
                                                               Consumer<Throwable> _errorMethodHandler,
                                                               Message<Object> _requestMessage,
                                                               ThrowableFutureConsumer<byte[]> _byteConsumer,
                                                               Consumer<Throwable> _errorHandler,
                                                               ThrowableErrorConsumer<Throwable, byte[]> _onFailureRespond,
                                                               DeliveryOptions _responseDeliveryOptions,
                                                               int _retryCount,
                                                               long _timeout,
                                                               long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_requestOptions).orElse(new DeliveryOptions());
        final RecursiveExecutor executor = (methodId,
                                            vertx,
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
                new ExecuteRSBasicByteResponse(methodId,
                        vertx, t,
                        errorMethodHandler,
                        requestMessage,
                        consumer,
                        null,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount, timeout, circuitBreakerTimeout).
                        execute();

        final RetryExecutor retry = (targetId,
                                     message,
                                     function,
                                     requestDeliveryOptions,
                                     methodId,
                                     vertx,
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
                mapToByteResponse(methodId,
                        targetId,
                        message,
                        function,
                        requestDeliveryOptions,
                        vertx, t,
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
        final ExecuteEventbusByteCall excecuteEventBusAndReply =
                (methodId, vertx,
                 errorMethodHandler,
                 requestMessage,
                 errorHandler,
                 onFailureRespond,
                 responseDeliveryOptions,
                 retryCount, timeout, circuitBreakerTimeout) ->
                        EventbusExecution.sendMessageAndSupplyHandler(
                                methodId,
                                _targetId,
                                _message,
                                _byteFunction,
                                deliveryOptions,
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


        return new ExecuteRSBasicByteResponse(_methodId, _vertx, _failure, _errorMethodHandler, _requestMessage, _byteConsumer, excecuteEventBusAndReply, _errorHandler,
                _onFailureRespond, _responseDeliveryOptions, _retryCount, _timeout, _circuitBreakerTimeout);
    }


}
