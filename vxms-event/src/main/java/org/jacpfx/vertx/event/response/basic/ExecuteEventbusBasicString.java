package org.jacpfx.vertx.event.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusStringCall;

import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusBasicString {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable failure;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final Message<Object> message;
    protected final ThrowableFutureConsumer<String> stringConsumer;
    protected final Consumer<Throwable> errorHandler;
    protected final ThrowableErrorConsumer<Throwable, String> onFailureRespond;
    protected final ExecuteEventbusStringCall excecuteEventBusAndReply;
    protected final DeliveryOptions deliveryOptions;
    protected final int retryCount;
    protected final long timeout;
    protected final long circuitBreakerTimeout;


    public ExecuteEventbusBasicString(String methodId,
                                      Vertx vertx, Throwable failure,
                                      Consumer<Throwable> errorMethodHandler,
                                      Message<Object> message,
                                      ThrowableFutureConsumer<String> stringConsumer,
                                      ExecuteEventbusStringCall excecuteEventBusAndReply,
                                      Consumer<Throwable> errorHandler,
                                      ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                      DeliveryOptions deliveryOptions,
                                      int retryCount, long timeout, long circuitBreakerTimeout) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.message = message;
        this.stringConsumer = stringConsumer;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.deliveryOptions = deliveryOptions;
        this.retryCount = retryCount;
        this.timeout = timeout;
        this.circuitBreakerTimeout = circuitBreakerTimeout;

    }

    /**
     * Execute the reply chain with given deliveryOptions
     *
     * @param deliveryOptions, the event bus Delivery Options
     */
    public void execute(DeliveryOptions deliveryOptions) {
        Objects.requireNonNull(deliveryOptions);
        new ExecuteEventbusBasicString(methodId, vertx, failure, errorMethodHandler, message, stringConsumer,
                excecuteEventBusAndReply, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout).execute();
    }


    /**
     * Execute the reply chain
     */
    public void execute() {
        vertx.runOnContext(action -> {
            ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(methodId,
                            vertx,
                            errorMethodHandler,
                            message,
                            errorHandler,
                            onFailureRespond,
                            deliveryOptions,
                            retryCount,
                            timeout,
                            circuitBreakerTimeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            ofNullable(stringConsumer).
                    ifPresent(userOperation -> {
                                int retry = retryCount;
                                ResponseExecution.createResponse(methodId,
                                        retry,
                                        timeout,
                                        circuitBreakerTimeout,
                                        userOperation,
                                        errorHandler,
                                        onFailureRespond,
                                        errorMethodHandler,
                                        vertx, failure, value -> {
                                            if (value.succeeded()) {
                                                respond(value.getResult());
                                            } else {
                                                fail(value.getCause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                                            }
                                        });

                            }
                    );

        });


    }


    protected void respond(String result) {
        if (result != null) {
            if (deliveryOptions != null) {
                message.reply(result, deliveryOptions);
            } else {
                message.reply(result);
            }
        }
    }

    protected void fail(String result, int statuscode) {
        if (result != null) message.fail(statuscode, result);
    }


}
