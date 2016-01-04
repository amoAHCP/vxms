package org.jacpfx.vertx.websocket.decoder;

import java.util.Optional;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public interface Decoder<O> {



    interface ByteDecoder<O>  extends Decoder<O> {
         Optional<O> decode(byte[] input);
    }

    interface StringDecoder<O> extends Decoder<O> {
         Optional<O> decode(String input);
    }
}
