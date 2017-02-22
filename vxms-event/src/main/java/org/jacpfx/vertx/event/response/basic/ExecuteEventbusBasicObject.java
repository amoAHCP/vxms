package org.jacpfx.vertx.event.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusObjectCall;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusBasicObject {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Message<Object> message;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final ThrowableFutureConsumer<Serializable> objectConsumer;
    protected final ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond;
    protected final ExecuteEventbusObjectCall excecuteEventBusAndReply;
    protected final Encoder encoder;
    protected final DeliveryOptions deliveryOptions;
    protected final int retryCount;
    protected final long timeout;
    protected final long circuitBreakerTimeout;

    public ExecuteEventbusBasicObject(String methodId,
                                      Vertx vertx,
                                      Throwable t,
                                      Consumer<Throwable> errorMethodHandler,
                                      Message<Object> message,
                                      ThrowableFutureConsumer<Serializable> objectConsumer,
                                      ExecuteEventbusObjectCall excecuteEventBusAndReply,
                                      Encoder encoder,
                                      Consumer<Throwable> errorHandler,
                                      ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
                                      DeliveryOptions deliveryOptions,
                                      int retryCount, long timeout, long circuitBreakerTimeout) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.message = message;
        this.objectConsumer = objectConsumer;
        this.encoder = encoder;
        this.deliveryOptions = deliveryOptions;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.retryCount = retryCount;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.timeout = timeout;
        this.circuitBreakerTimeout = circuitBreakerTimeout;
    }


    /**
     * Execute the reply chain with given http status code and content-type
     *
     * @param deliveryOptions, the eventbus delivery options
     */
    public void execute(DeliveryOptions deliveryOptions) {
        Objects.requireNonNull(deliveryOptions);
        final ExecuteEventbusBasicObject lastStep = new ExecuteEventbusBasicObject(methodId, vertx, t, errorMethodHandler, message, objectConsumer, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
        lastStep.execute();
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
                            encoder,
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
            ofNullable(objectConsumer).
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
                                        vertx, t, value -> {
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


    protected void fail(String result, int statuscode) {
        if (result != null) message.fail(statuscode, result);

    }

    protected void respond(Serializable result) {
        if (result != null) {
            ResponseExecution.encode(result, encoder).ifPresent(value -> {
                if (deliveryOptions != null) {
                    message.reply(value, deliveryOptions);
                } else {
                    message.reply(value);
                }
            });
        }
    }


}
