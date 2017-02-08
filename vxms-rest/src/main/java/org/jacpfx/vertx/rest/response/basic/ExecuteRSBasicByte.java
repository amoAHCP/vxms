package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventBusByteCall;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * Created by Andy Moncsek on 12.01.16. This class is the end of the fluent API, all data collected to execute the chain.
 */
public class ExecuteRSBasicByte {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable failure;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final ThrowableFutureConsumer<byte[]> byteConsumer;
    protected final ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond;
    protected final ExecuteEventBusByteCall excecuteEventBusAndReply;
    protected final Encoder encoder;
    protected final int httpStatusCode;
    protected final int httpErrorCode;
    protected final int retryCount;
    protected final long timeout;
    protected final long circuitBreakerTimeout;

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                 the method identifier
     * @param vertx                    the vertx instance
     * @param failure                  the failure thrown while task execution
     * @param errorMethodHandler       the error handler
     * @param context                  the vertx routing context
     * @param headers                  the headers to pass to the response
     * @param byteConsumer             the consumer that takes a Future to complete, producing the byte response
     * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                  the encoder to encode your objects
     * @param errorHandler             the error handler
     * @param onFailureRespond         the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode           the http status code to set for response
     * @param httpErrorCode            the http error code to set in case of failure handling
     * @param retryCount               the amount of retries before failure execution is triggered
     * @param timeout                  the amount of time before the execution will be aborted
     * @param circuitBreakerTimeout    the amount of time before the circuit breaker closed again
     */
    public ExecuteRSBasicByte(String methodId,
                              Vertx vertx,
                              Throwable failure,
                              Consumer<Throwable> errorMethodHandler,
                              RoutingContext context,
                              Map<String, String> headers,
                              ThrowableFutureConsumer<byte[]> byteConsumer,
                              ExecuteEventBusByteCall excecuteEventBusAndReply,
                              Encoder encoder,
                              Consumer<Throwable> errorHandler,
                              ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                              int httpStatusCode,
                              int httpErrorCode,
                              int retryCount,
                              long timeout,
                              long circuitBreakerTimeout) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.byteConsumer = byteConsumer;
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
        new ExecuteRSBasicByte(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteConsumer,
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
     * @param status,     the http status code
     * @param contentType , the html content-type
     */
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        new ExecuteRSBasicByte(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                ResponseExecution.updateContentType(headers, contentType),
                byteConsumer,
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
        new ExecuteRSBasicByte(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                ResponseExecution.updateContentType(headers, contentType),
                byteConsumer,
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
        vertx.runOnContext(action -> {
            ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx,
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
                                        vertx,
                                        failure,
                                        value -> {
                                            if (value.succeeded()) {
                                                if (!value.handledError()) {
                                                    respond(value.getResult());
                                                } else {
                                                    respond(value.getResult(), httpErrorCode);
                                                }

                                            } else {
                                                respond(value.getCause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                                            }
                                            checkAndCloseResponse(retry);
                                        }, retry, timeout, circuitBreakerTimeout);
                            }
                    );
        });

    }

    protected void checkAndCloseResponse(int retry) {
        final HttpServerResponse response = context.response();
        if (retry == 0 && !response.ended()) {
            response.end();
        }
    }

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


    protected void respond(String result, int statuscode) {
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

    protected void respond(byte[] result) {
        respond(result, httpStatusCode);
    }


}
