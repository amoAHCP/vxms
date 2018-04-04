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

package org.jacpfx.vxms.k8s.util;

/**
 * Simple String util
 * Created by amo on 06.04.17.
 */
public class StringUtil {

  /**
   * Determine if a string is {@code null} or {@link String#isEmpty()} returns {@code true}.
   *
   * @param s, the String to test
   * @return true if String is null or empty
   */
  public static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
