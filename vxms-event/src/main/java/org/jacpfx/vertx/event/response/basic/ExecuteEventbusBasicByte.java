package org.jacpfx.vertx.event.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusByteCall;

import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusBasicByte {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Message<Object> message;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final ThrowableFutureConsumer<byte[]> byteConsumer;
    protected final ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond;
    protected final ExecuteEventbusByteCall excecuteEventBusAndReply;
    protected final DeliveryOptions deliveryOptions;
    protected final int retryCount;
    protected final long timeout;
    protected final long circuitBreakerTimeout;

    public ExecuteEventbusBasicByte(String methodId,
                                    Vertx vertx,
                                    Throwable t,
                                    Consumer<Throwable> errorMethodHandler,
                                    Message<Object> message,
                                    ThrowableFutureConsumer<byte[]> byteConsumer,
                                    ExecuteEventbusByteCall excecuteEventBusAndReply,
                                    Consumer<Throwable> errorHandler,
                                    ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                    DeliveryOptions deliveryOptions,
                                    int retryCount,
                                    long timeout,
                                    long circuitBreakerTimeout) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.message = message;
        this.byteConsumer = byteConsumer;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.deliveryOptions = deliveryOptions;
        this.retryCount = retryCount;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.timeout = timeout;
        this.circuitBreakerTimeout = circuitBreakerTimeout;
    }

    /**
     * Execute the reply chain with given http status code
     *
     * @param deliveryOptions, the event b us deliver options
     */
    public void execute(DeliveryOptions deliveryOptions) {
        Objects.requireNonNull(deliveryOptions);
        new ExecuteEventbusBasicByte(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply,
                errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout).execute();
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

            ofNullable(byteConsumer).
                    ifPresent(userOperation -> {
                                int retry = retryCount;
                                ResponseExecution.createResponse(methodId,
                                        retry,
                                        timeout,
                                        circuitBreakerTimeout,
                                        userOperation, errorHandler,
                                        onFailureRespond, errorMethodHandler,
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

    protected void respond(byte[] result) {
        if (result != null) {
            if (deliveryOptions != null) {
                message.reply(result, deliveryOptions);
            } else {
                message.reply(result);
            }
        }
    }


}
