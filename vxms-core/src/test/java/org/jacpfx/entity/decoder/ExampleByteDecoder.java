package org.jacpfx.entity.decoder;

import java.util.Optional;
import org.jacpfx.vxms.common.decoder.Decoder;

/**
 * Created by Andy Moncsek on 18.11.15.
 */
public class ExampleByteDecoder implements Decoder.ByteDecoder<String> {

  @Override
  public Optional<String> decode(byte[] input) {
    return Optional.of(new String(input));
  }
}
