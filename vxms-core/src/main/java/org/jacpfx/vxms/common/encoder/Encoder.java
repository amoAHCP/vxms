/*
 * Copyright [2018] [Andy Moncsek]
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

package org.jacpfx.vxms.common.encoder;

/**
 * Created by Andy Moncsek on 17.11.15.
 * The encoder interface
 */
public interface Encoder {

  /**
   * Encode to byte array
   *
   * @param <I> the type of the object to encode
   */
  interface ByteEncoder<I> extends Encoder {

    /**
     * encode an object to byte array
     *
     * @param input the object
     * @return the byte array
     */
    byte[] encode(I input);
  }

  /**
   * Encode to string
   *
   * @param <I> the type of the object to encode
   */
  interface StringEncoder<I> extends Encoder {

    /**
     * Encode to String
     *
     * @param input the object
     * @return the string
     */
    String encode(I input);
  }
}
