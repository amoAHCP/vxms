package org.jacpfx.vertx.websocket.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jacpfx.common.CustomSupplier;
import org.jacpfx.vertx.websocket.decoder.Decoder;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 17.11.15.
 */
public class WebSocketHandler {
    private final WebSocketEndpoint endpoint;
    private final Vertx vertx;
    private final WebSocketRegistry registry;
    private final Consumer<Throwable> errorMethodHandler;
    private final byte[] value;


    public WebSocketHandler(WebSocketRegistry registry, WebSocketEndpoint endpoint, byte[] value, Vertx vertx, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.value = value;
        this.errorMethodHandler = errorMethodHandler;
    }

    /**
     * Returns the Endpoint definition with URL and handler ids
     *
     * @return {@see WSEndpoint}
     */
    public WebSocketEndpoint endpoint() {
        return this.endpoint;
    }

    /**
     * Returns the payload handler which gives you access to the payload transmitted
     *
     * @return {@see Payload}
     */
    public Payload payload() {
        return new Payload(value);
    }


    public TargetType response() {
        return new TargetType(endpoint, vertx, registry, errorMethodHandler, false);
    }

    public class Payload {
        private final byte[] value;

        private Payload(byte[] value) {
            this.value = value;
        }

        public Optional<String> getString() {
            return Optional.ofNullable(value != null ? new String(value) : null);
        }

        public Optional<byte[]> getBytes() {
            return Optional.ofNullable(value);
        }


        public <T> Optional<T> getObject(Class<T> clazz, Decoder decoder) {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(decoder);
            try {
                if (decoder instanceof Decoder.ByteDecoder) {
                    return ((Decoder.ByteDecoder<T>) decoder).decode(value);
                } else if (decoder instanceof Decoder.StringDecoder) {
                    return ((Decoder.StringDecoder<T>) decoder).decode(new String(value));
                }
            } catch (ClassCastException e) {
                return Optional.empty();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Optional.empty();
        }
    }

    public class TargetType {
        protected final WebSocketEndpoint endpoint;
        protected final Vertx vertx;
        protected final WebSocketRegistry registry;
        protected final Consumer<Throwable> errorMethodHandler;
        protected final boolean async;

        private TargetType(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler, boolean async) {
            this.endpoint = endpoint;
            this.vertx = vertx;
            this.registry = registry;
            this.errorMethodHandler = errorMethodHandler;
            this.async = async;
        }

        public TargetTypeAsync async() {
            return new TargetTypeAsync(endpoint, vertx, registry, errorMethodHandler, true);
        }

        public ResponseType toAll() {
            return new ResponseType(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler);
        }

        public ResponseType toAllBut(WebSocketEndpoint... endpoint) {
            // TODO iteration over stream / filter
            return new ResponseType(endpoint, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler);
        }

        public ResponseType toCaller() {
            return new ResponseType(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler);
        }

        public ResponseType to(WebSocketEndpoint... endpoint) {
            return new ResponseType(endpoint, vertx, CommType.TO, errorMethodHandler);
        }


    }

    public class TargetTypeAsync extends TargetType {


        private TargetTypeAsync(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler, boolean async) {
            super(endpoint, vertx, registry, errorMethodHandler, async);
        }


        @Override
        public ResponseTypeAsync toAll() {
            return new ResponseTypeAsync(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler);
        }

        @Override
        public ResponseTypeAsync toAllBut(WebSocketEndpoint... endpoint) {
            // TODO iteration over stream / filter  .
            return new ResponseTypeAsync(endpoint, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler);
        }

        @Override
        public ResponseTypeAsync toCaller() {
            return new ResponseTypeAsync(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler);
        }

        @Override
        public ResponseTypeAsync to(WebSocketEndpoint... endpoint) {
            return new ResponseTypeAsync(endpoint, vertx, CommType.TO, errorMethodHandler);
        }


    }

    public class ResponseType {
        protected final WebSocketEndpoint[] endpoint;
        protected final Vertx vertx;
        protected final CommType commType;
        protected final Consumer<Throwable> errorMethodHandler;

        private ResponseType(WebSocketEndpoint[] endpoint, Vertx vertx, final CommType commType, Consumer<Throwable> errorMethodHandler) {
            this.endpoint = endpoint;
            this.vertx = vertx;
            this.commType = commType;
            this.errorMethodHandler = errorMethodHandler;
        }

        public ExecuteWSBasicResponse byteResponse(CustomSupplier<byte[]> byteSupplier) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, null, null, null, null, errorMethodHandler, null, null, null, 0, 0L);
        }

        public ExecuteWSBasicResponse stringResponse(CustomSupplier<String> stringSupplier) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, null, stringSupplier, null, null, null, errorMethodHandler, null, null, null, 0, 0L);
        }

        public ExecuteWSBasicResponse objectResponse(CustomSupplier<Serializable> objectSupplier, Encoder encoder) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, null, null, objectSupplier, encoder, null, errorMethodHandler, null, null, null, 0, 0L);
        }
    }

    public class ResponseTypeAsync extends ResponseType {


        private ResponseTypeAsync(WebSocketEndpoint[] endpoint, Vertx vertx, final CommType commType, Consumer<Throwable> errorMethodHandler) {
            super(endpoint, vertx, commType, errorMethodHandler);
        }

        @Override
        public ExecuteWSResponse byteResponse(CustomSupplier<byte[]> byteSupplier) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, null, null, null, null, errorMethodHandler, null, null, null, 0, 0L);
        }

        @Override
        public ExecuteWSResponse stringResponse(CustomSupplier<String> stringSupplier) {
            return new ExecuteWSResponse(endpoint, vertx, commType, null, stringSupplier, null, null, null, errorMethodHandler, null, null, null, 0, 0L);
        }

        @Override
        public ExecuteWSResponse objectResponse(CustomSupplier<Serializable> objectSupplier, Encoder encoder) {
            return new ExecuteWSResponse(endpoint, vertx, commType, null, null, objectSupplier, encoder, null, errorMethodHandler, null, null, null, 0, 0L);
        }
    }

    public class ExecuteWSResponse extends ExecuteWSBasicResponse {


        private ExecuteWSResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, CustomSupplier<byte[]> byteSupplier, CustomSupplier<String> stringSupplier, CustomSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, byte[]> errorHandlerByte, Function<Throwable, String> errorHandlerString, Function<Throwable, Serializable> errorHandlerObject, int retryCount, long timeout) {
            super(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);

        }

        @Override
        public ExecuteWSResponse onError(Consumer<Throwable> errorHandler) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        @Override
        public ExecuteWSResponse onByteResponseError(Function<Throwable, byte[]> errorHandlerByte) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        @Override
        public ExecuteWSResponse onStringResponseError(Function<Throwable, String> errorHandlerString) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        @Override
        public ExecuteWSResponse onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        @Override
        public ExecuteWSResponse retry(int retryCount) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        // TODO implement delay, but throw exception when not in async mode!
        public ExecuteWSResponse delay(int retryCount) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        public ExecuteWSResponse timeout(long timeout) {
            return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        @Override
        public void execute() {
            int retry = retryCount > 0 ? retryCount : 0;
            Optional.ofNullable(byteSupplier).
                    ifPresent(supplier ->
                            this.vertx.executeBlocking(handler ->
                                    WebSocketExecutionUtil.executeRetryAndCatchAsync(supplier, handler, new byte[0], errorHandler, errorHandlerByte, vertx, retry, timeout), false, (Handler<AsyncResult<byte[]>>) result ->
                                    WebSocketExecutionUtil.handleExecutionResult(result, errorMethodHandler, () -> Optional.ofNullable(result.result()).ifPresent(value -> WebSocketExecutionUtil.sendBinary(commType, vertx, registry, endpoint, value)))
                            ));


            Optional.ofNullable(stringSupplier).
                    ifPresent(supplier ->
                            this.vertx.executeBlocking(handler ->
                                    WebSocketExecutionUtil.executeRetryAndCatchAsync(supplier, handler, "", errorHandler, errorHandlerString, vertx, retry, timeout), false, (Handler<AsyncResult<String>>) result ->
                                    WebSocketExecutionUtil.handleExecutionResult(result, errorMethodHandler, () -> Optional.ofNullable(result.result()).ifPresent(value -> WebSocketExecutionUtil.sendText(commType, vertx, registry, endpoint, value)))
                            ));


            Optional.ofNullable(objectSupplier).
                    ifPresent(supplier ->
                            this.vertx.executeBlocking(handler ->
                                    WebSocketExecutionUtil.executeRetryAndCatchAsync(supplier, handler, "", errorHandler, errorHandlerObject, vertx, retry, timeout), false, (Handler<AsyncResult<Serializable>>) result ->
                                    WebSocketExecutionUtil.handleExecutionResult(result, errorMethodHandler, () -> Optional.ofNullable(result.result()).ifPresent(value -> WebSocketExecutionUtil.encode(value, encoder).ifPresent(val -> {
                                        WebSocketExecutionUtil.sendObjectResult(val, commType, vertx, registry, endpoint);
                                    })))
                            ));

        }


    }


    public class ExecuteWSBasicResponse {
        protected final WebSocketEndpoint[] endpoint;
        protected final Vertx vertx;
        protected final CommType commType;
        protected final CustomSupplier<byte[]> byteSupplier;
        protected final CustomSupplier<String> stringSupplier;
        protected final CustomSupplier<Serializable> objectSupplier;
        protected final Encoder encoder;
        protected final Consumer<Throwable> errorHandler;
        protected final Consumer<Throwable> errorMethodHandler;
        protected final Function<Throwable, byte[]> errorHandlerByte;
        protected final Function<Throwable, String> errorHandlerString;
        protected final Function<Throwable, Serializable> errorHandlerObject;
        protected final int retryCount;
        protected final long timeout;

        private ExecuteWSBasicResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, CustomSupplier<byte[]> byteSupplier, CustomSupplier<String> stringSupplier, CustomSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, byte[]> errorHandlerByte, Function<Throwable, String> errorHandlerString, Function<Throwable, Serializable> errorHandlerObject, int retryCount, long timeout) {
            this.endpoint = endpoint;
            this.vertx = vertx;
            this.commType = commType;
            this.byteSupplier = byteSupplier;
            this.stringSupplier = stringSupplier;
            this.objectSupplier = objectSupplier;
            this.encoder = encoder;
            this.errorHandler = errorHandler;
            this.errorMethodHandler = errorMethodHandler;
            this.errorHandlerByte = errorHandlerByte;
            this.errorHandlerString = errorHandlerString;
            this.errorHandlerObject = errorHandlerObject;
            this.retryCount = retryCount;
            this.timeout = timeout;
        }

        public ExecuteWSBasicResponse onError(Consumer<Throwable> errorHandler) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        public ExecuteWSBasicResponse onByteResponseError(Function<Throwable, byte[]> errorHandlerByte) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        public ExecuteWSBasicResponse onStringResponseError(Function<Throwable, String> errorHandlerString) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }

        public ExecuteWSBasicResponse onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }


        public ExecuteWSBasicResponse retry(int retryCount) {
            return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount, timeout);
        }


        public void execute() {
            int retry = retryCount > 0 ? retryCount : 0;
            Optional.ofNullable(byteSupplier).
                    ifPresent(supplier -> {
                        byte[] result = WebSocketExecutionUtil.executeRetryAndCatch(supplier, null, errorHandler, errorHandlerByte, errorMethodHandler, retry);

                        Optional.ofNullable(result).ifPresent(value -> WebSocketExecutionUtil.sendBinary(commType, vertx, registry, endpoint, value));
                    });
            Optional.ofNullable(stringSupplier).
                    ifPresent(supplier -> {
                        String result = WebSocketExecutionUtil.executeRetryAndCatch(supplier, null, errorHandler, errorHandlerString, errorMethodHandler, retry);

                        Optional.ofNullable(result).ifPresent(value -> WebSocketExecutionUtil.sendText(commType, vertx, registry, endpoint, value));
                    });

            Optional.ofNullable(objectSupplier).
                    ifPresent(supplier -> {
                        Serializable result = WebSocketExecutionUtil.executeRetryAndCatch(supplier, null, errorHandler, errorHandlerObject, errorMethodHandler, retry);

                        Optional.ofNullable(result).ifPresent(value -> WebSocketExecutionUtil.encode(value, encoder).ifPresent(val -> WebSocketExecutionUtil.sendObjectResult(val, commType, vertx, registry, endpoint)));
                    });
        }


    }
}
