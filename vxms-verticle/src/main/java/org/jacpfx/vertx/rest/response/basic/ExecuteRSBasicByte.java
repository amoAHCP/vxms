package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.rest.util.ResponseUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByte {
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final ThrowableSupplier<byte[]> byteSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, byte[]> onFailureRespond;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final ExecuteEventBusByteCall excecuteEventBusAndReply;

    public ExecuteRSBasicByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<byte[]> byteSupplier, ExecuteEventBusByteCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> onFailureRespond, int httpStatusCode, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.byteSupplier = byteSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
    }

    /**
     * Execute the reply chain with given http status code
     *
     * @param status, the http status code
     */
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSBasicByte lastStep = new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount);
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
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSBasicByte lastStep = new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headerMap, byteSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount);
        lastStep.execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        final Map<String, String> headerMap = updateContentMap(contentType);
        final ExecuteRSBasicByte lastStep = new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headerMap, byteSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
        lastStep.execute();
    }

    protected Map<String, String> updateContentMap(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return headerMap;
    }


    /**
     * Execute the reply chain
     */
    public void execute() {
        vertx.runOnContext(action -> {
            // excecuteEventBusAndReply & stringSupplier never non null at the same time
            Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            Optional.ofNullable(byteSupplier).
                    ifPresent(supplier -> {
                                int retry = retryCount;
                                byte[] result = null;
                                Optional.
                                        ofNullable(ResponseUtil.
                                                createResponse(retry, result, supplier, errorHandler, onFailureRespond, errorMethodHandler)).
                                        ifPresent(res -> repond(res));

                            }
                    );
        });

    }

    protected void repond(byte[] result) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            RESTExecutionUtil.updateResponseHaders(headers, response);
            RESTExecutionUtil.updateResponseStatusCode(httpStatusCode, response);
            if (result != null) {
                response.end(Buffer.buffer(result));
            } else {
                response.end();
            }
        }
    }


}
