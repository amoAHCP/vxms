package org.jacpfx;

import org.jacpfx.common.util.Serializer;
import org.jacpfx.vertx.websocket.decoder.Decoder;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public class ExampleByteDecoderMyTest implements Decoder.ByteDecoder<MyTestObject> {
    @Override
    public Optional<MyTestObject> decode(byte[] input) {
        try {
            MyTestObject result = (MyTestObject)Serializer.deserialize(input);
            return Optional.ofNullable(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
