package org.jacpfx.vertx.websocket.decoder;

import java.util.Optional;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public interface Decoder{



    interface ByteDecoder<O> extends Decoder{
         Optional<O> decode(byte[] input);
    }

    interface StringDecoder<O> extends Decoder{
         Optional<O> decode(String input);
    }
}
