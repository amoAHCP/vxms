package org.jacpfx.vertx.websocket.response;

import org.jacpfx.common.decoder.Decoder;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by Andy Moncsek on 17.12.15.
 * The Payload class contains the payload itself and methods to get access to
 */
public class Payload {
    private final byte[] value;

    protected Payload(byte[] value) {
        this.value = value;
    }

    /**
     * returns an Optional with the String payload
     *
     * @return the String payload
     */
    public Optional<String> getString() {
        return Optional.ofNullable(value != null ? new String(value) : null);
    }

    /**
     * returns an Optional with the byte array payload
     *
     * @return the byte array payload
     */
    public Optional<byte[]> getBytes() {
        return Optional.ofNullable(value);
    }

    /**
     * returns an Optional with the typed Object payload
     *
     * @param decoder the decoder {@see org.jacpfx.common.decoder.Decoder}
     * @param <T>     the type of the expected payload
     * @return the Optional with the expected payload
     */
    public <T> Optional<T> getObject(Decoder<T> decoder) {
        Objects.requireNonNull(decoder);
        try {
            if (decoder instanceof Decoder.ByteDecoder) {
                return ((Decoder.ByteDecoder<T>) decoder).decode(value);
            } else if (decoder instanceof Decoder.StringDecoder) {
                return ((Decoder.StringDecoder<T>) decoder).decode(new String(value));
            }
        } catch (ClassCastException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}

