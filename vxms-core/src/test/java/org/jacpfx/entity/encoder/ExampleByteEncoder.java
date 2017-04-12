package org.jacpfx.entity.encoder;

import java.io.IOException;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.Payload;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ExampleByteEncoder implements Encoder.ByteEncoder<Payload<String>> {

  @Override
  public byte[] encode(Payload<String> input) {
    try {
      return Serializer.serialize(input);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new byte[0];
  }
}
