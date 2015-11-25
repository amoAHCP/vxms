package org.jacpfx;

import org.jacpfx.vertx.websocket.decoder.Decoder;

import java.util.Optional;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public class ExampleByteDecoder implements Decoder.ByteDecoder<String> {
    @Override
    public Optional<String> decode(byte[] input) {
        return Optional.ofNullable(new String(input));
    }
}
