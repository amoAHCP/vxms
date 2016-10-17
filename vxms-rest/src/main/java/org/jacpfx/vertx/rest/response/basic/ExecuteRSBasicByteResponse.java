package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByteResponse extends ExecuteRSBasicByte {


    public ExecuteRSBasicByteResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<byte[]> byteConsumer, ExecuteEventBusByteCall excecuteEventBusAndReply, Encoder encoder,
                                      Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int retryCount, long timeout) {
        super(vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSBasicByte onFailureRespond(ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond) {
        return new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
    }

    /**
     * This is an intermediate error method, the error will be passed along the chain (onFailurePass or simply an error)
     *
     * @param errorHandler , a consumer that holds the error
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout
     * @return
     */

    public ExecuteRSBasicByteResponse timeout(long timeout) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse retry(int retryCount) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
    }

    /**
     * put HTTP header to response
     *
     * @param key   the header name
     * @param value the header value
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headerMap, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
    }
}
