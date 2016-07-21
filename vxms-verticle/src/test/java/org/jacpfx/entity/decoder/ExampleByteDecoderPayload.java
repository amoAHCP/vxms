package org.jacpfx.entity.decoder;

import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;
import org.jacpfx.common.decoder.Decoder;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public class ExampleByteDecoderPayload implements Decoder.ByteDecoder<Payload<String>> {
    @Override
    public Optional<Payload<String>> decode(byte[] input) {
        try {
            return Optional.ofNullable((Payload<String>)Serializer.deserialize(input));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
