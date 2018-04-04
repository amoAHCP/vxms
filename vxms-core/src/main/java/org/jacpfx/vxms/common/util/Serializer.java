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

package org.jacpfx.vxms.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by amo on 04.12.14.
 * Serializing util
 */
public class Serializer {

  /**
   * Serialize an object
   *
   * @param obj the object to serialize
   * @return the byte array of the serialized object
   * @throws IOException the possible io exception
   */
  public static byte[] serialize(Object obj) throws IOException {
    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    final ObjectOutputStream o = new ObjectOutputStream(b);
    o.writeObject(obj);
    o.close();
    return b.toByteArray();
  }

  /**
   * Deserialize an object
   *
   * @param bytes the byte array to deserialize
   * @return the object
   * @throws IOException deserialization exception
   * @throws ClassNotFoundException if class not found
   */
  public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
    final ObjectInputStream o = new ObjectInputStream(b);
    return o.readObject();
  }


}