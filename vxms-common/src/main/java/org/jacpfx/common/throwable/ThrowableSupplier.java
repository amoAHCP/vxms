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

package org.jacpfx.common.throwable;

/**
 * Created by Andy Moncsek on 27.11.15.
 * A supplier that throws a throwable, so vxms can handle the exceptions
 */
public interface ThrowableSupplier<T> {

  /**
   * the supplier method
   *
   * @return the value
   * @throws Throwable the throwable
   */
  T get() throws Throwable;
}
