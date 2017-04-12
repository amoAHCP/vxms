package org.jacpfx.entity.decoder;

import java.io.IOException;
import java.util.Optional;
import org.jacpfx.common.decoder.Decoder;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.MyTestObject;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public class ExampleByteDecoderMyTest implements Decoder.ByteDecoder<MyTestObject> {

  @Override
  public Optional<MyTestObject> decode(byte[] input) {
    try {
      MyTestObject result = (MyTestObject) Serializer.deserialize(input);
      return Optional.ofNullable(result);
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}
