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

package org.jacpfx.vxms.common.decoder;

import java.util.Optional;

/**
 * Created by Andy Moncsek on 18.11.15.
 * The decoder interface
 */
public interface Decoder<O> {


  /**
   * Decode from byte array
   *
   * @param <O> the type of the object to decode
   */
  interface ByteDecoder<O> extends Decoder<O> {

    /**
     * decode the input
     *
     * @param input the byte array input
     * @return an optional with the result
     */
    Optional<O> decode(byte[] input);
  }

  /**
   * Decode from String
   *
   * @param <O> the type of the object to decode
   */
  interface StringDecoder<O> extends Decoder<O> {

    /**
     * decode the input
     *
     * @param input the String input
     * @return an optional with the result
     */
    Optional<O> decode(String input);
  }
}
