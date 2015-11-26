package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.jacpfx.vertx.websocket.decoder.Decoder;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 17.11.15.
 */
public class WebSocketHandler {
    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool(); // TODO use fixed size and get amount of vertcle instances
    private final WebSocketEndpoint endpoint;
    private final Vertx vertx;
    private final WebSocketRegistry registry;
    private byte[] value;


    public WebSocketHandler(WebSocketRegistry registry, WebSocketEndpoint endpoint, byte[] value, Vertx vertx) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.value = value;
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
        return new TargetType(endpoint, vertx, false);
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
        private final WebSocketEndpoint endpoint;
        private final Vertx vertx;
        private final boolean async;

        private TargetType(WebSocketEndpoint endpoint, Vertx vertx, boolean async) {
            this.endpoint = endpoint;
            this.vertx = vertx;
            this.async = async;
        }

        public TargetType async() {
            return new TargetType(endpoint, vertx, true);
        }

        public ResponseType toAll() {
            return new ResponseType(new WebSocketEndpoint[]{endpoint}, vertx, async, CommType.ALL);
        }

        public ResponseType toAllBut(WebSocketEndpoint... endpoint) {
            // TODO iteration over stream / filter
            return new ResponseType(endpoint, vertx, async, CommType.ALL_BUT_CALLER);
        }

        public ResponseType toCaller() {
            return new ResponseType(new WebSocketEndpoint[]{endpoint}, vertx, async, CommType.CALLER);
        }

        public ResponseType to(WebSocketEndpoint... endpoint) {
            return new ResponseType(endpoint, vertx, async, CommType.TO);
        }


    }

    public class ResponseType {
        private final WebSocketEndpoint[] endpoint;
        private final Vertx vertx;
        private final boolean async;
        private final CommType commType;

        private ResponseType(WebSocketEndpoint[] endpoint, Vertx vertx, final boolean async, final CommType commType) {
            this.endpoint = endpoint;
            this.vertx = vertx;
            this.async = async;
            this.commType = commType;
        }

        public ExecuteWSResponse byteResponse(Supplier<byte[]> byteSupplier) {
            return new ExecuteWSResponse(endpoint, vertx, async, commType, byteSupplier, null, null, null);
        }

        public ExecuteWSResponse stringResponse(Supplier<String> stringSupplier) {
            return new ExecuteWSResponse(endpoint, vertx, async, commType, null, stringSupplier, null, null);
        }

        public ExecuteWSResponse objectResponse(Supplier<Serializable> objectSupplier, Encoder encoder) {
            return new ExecuteWSResponse(endpoint, vertx, async, commType, null, null, objectSupplier, encoder);
        }
    }

    public class ExecuteWSResponse {
        private final WebSocketEndpoint[] endpoint;
        private final Vertx vertx;
        private final boolean async;
        private final CommType commType;
        private final Supplier<byte[]> byteSupplier;
        private final Supplier<String> stringSupplier;
        private final Supplier<Serializable> objectSupplier;
        private final Encoder encoder;

        private ExecuteWSResponse(WebSocketEndpoint[] endpoint, Vertx vertx, boolean async, CommType commType, Supplier<byte[]> byteSupplier, Supplier<String> stringSupplier, Supplier<Serializable> objectSupplier, Encoder encoder) {
            this.endpoint = endpoint;
            this.vertx = vertx;
            this.async = async;
            this.commType = commType;
            this.byteSupplier = byteSupplier;
            this.stringSupplier = stringSupplier;
            this.objectSupplier = objectSupplier;
            this.encoder = encoder;
        }

        public void execute() {
            if (async) {
                Optional.ofNullable(byteSupplier).
                        ifPresent(supplier -> CompletableFuture.supplyAsync(byteSupplier, EXECUTOR).thenAccept(this::sendBinary));
                Optional.ofNullable(stringSupplier).
                        ifPresent(supplier -> CompletableFuture.supplyAsync(stringSupplier, EXECUTOR).thenAccept(this::sendText));
                Optional.ofNullable(objectSupplier).
                        ifPresent(supplier -> CompletableFuture.supplyAsync(objectSupplier, EXECUTOR).thenAccept(value -> serialize(value, encoder).ifPresent(val -> {
                            if (val instanceof String) {
                                sendText((String) val);
                            } else {
                                sendBinary((byte[]) val);
                            }
                        })));
            } else {
                // TODO check for exception, think about @OnError method execution
                Optional.ofNullable(byteSupplier).
                        ifPresent(supplier -> Optional.ofNullable(supplier.get()).ifPresent(this::sendBinary));
                // TODO check for exception, think about @OnError method execution
                Optional.ofNullable(stringSupplier).
                        ifPresent(supplier -> Optional.ofNullable(supplier.get()).ifPresent(this::sendText));
                // TODO check for exception, think about @OnError method execution
                Optional.ofNullable(objectSupplier).
                        ifPresent(supplier -> Optional.ofNullable(supplier.get()).ifPresent(value -> serialize(value, encoder).ifPresent(val -> {
                            if (val instanceof String) {
                                sendText((String) val);
                            } else {
                                sendBinary((byte[]) val);
                            }
                        })));

            }
        }



        // TODO handle exceptions
        private Optional<?> serialize(Serializable value, Encoder encoder) {
            if (encoder instanceof Encoder.ByteEncoder) {
                return Optional.ofNullable(((Encoder.ByteEncoder) encoder).encode(value));
            } else if (encoder instanceof Encoder.StringEncoder) {
                return Optional.ofNullable(((Encoder.StringEncoder) encoder).encode(value));
            }

            return Optional.empty();
        }

        private void sendText(String value) {
            switch (commType) {

                case ALL:
                    registry.findEndpointsAndExecute(endpoint[0], match -> vertx.eventBus().send(match.getTextHandlerId(), value));
                    break;
                case ALL_BUT_CALLER:
                    registry.findEndpointsAndExecute(endpoint[0], match -> {
                        if (!endpoint[0].equals(match)) vertx.eventBus().send(match.getTextHandlerId(), value);
                    });
                    break;
                case CALLER:
                    vertx.eventBus().send(endpoint[0].getTextHandlerId(), value);
                    break;
                case TO:
                    Stream.of(endpoint).forEach(ep -> vertx.eventBus().send(ep.getTextHandlerId(), value));
                    break;
            }
        }

        private void sendBinary(byte[] value) {
            switch (commType) {

                case ALL:
                    registry.findEndpointsAndExecute(endpoint[0], match -> vertx.eventBus().send(match.getBinaryHandlerId(), Buffer.buffer(value)));
                    break;
                case ALL_BUT_CALLER:
                    registry.findEndpointsAndExecute(endpoint[0], match -> {
                        if (!endpoint[0].equals(match))
                            vertx.eventBus().send(match.getTextHandlerId(), Buffer.buffer(value));
                    });
                    break;
                case CALLER:
                    vertx.eventBus().send(endpoint[0].getBinaryHandlerId(), Buffer.buffer(value));
                    break;
                case TO:
                    Stream.of(endpoint).forEach(ep -> vertx.eventBus().send(ep.getBinaryHandlerId(), Buffer.buffer(value)));
                    break;
            }
        }
    }

    public enum CommType {
        ALL, ALL_BUT_CALLER, CALLER, TO
    }
}
