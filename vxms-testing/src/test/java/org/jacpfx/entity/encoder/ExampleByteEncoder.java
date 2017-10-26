/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.entity.encoder;

import java.io.IOException;
import org.jacpfx.entity.Payload;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.util.Serializer;

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
