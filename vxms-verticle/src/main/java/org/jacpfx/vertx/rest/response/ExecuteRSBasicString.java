package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicString {
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, String> errorHandlerString;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final ExecuteEventBusStringCall excecuteEventBusAndReply;


    public ExecuteRSBasicString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.stringSupplier = stringSupplier;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorHandlerString = errorHandlerString;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Execute the reply chain with given http status code
     *
     * @param status, the http status code
     */
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSBasicString lastStep = new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, status.code(), retryCount);
        lastStep.execute();
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
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        final ExecuteRSBasicString lastStep = new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headerMap, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, status.code(), retryCount);
        lastStep.execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        final ExecuteRSBasicString lastStep = new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headerMap, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
        lastStep.execute();
    }

    /**
     * Execute the reply chain
     */
    public void execute() {
        Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
            try {
                evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
            } catch (Exception e) {
                System.out.println("EXCEPTION ::::::");
                e.printStackTrace();
            }

        });

        Optional.ofNullable(stringSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            String result = null;
                            boolean errorHandling = false;
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionUtil.handleError(result, errorHandler, errorHandlerString, errorMethodHandler, e);
                                        errorHandling = true;
                                    } else {
                                        RESTExecutionUtil.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (errorHandling && result == null) return;
                            repond(result);

                        }
                );


    }

    protected void repond(String result) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            RESTExecutionUtil.updateResponseHaders(headers, response);
            RESTExecutionUtil.updateResponseStatusCode(httpStatusCode, response);
            if (result != null) {
                response.end(result);
            } else {
                response.end();
            }
        }
    }


}
