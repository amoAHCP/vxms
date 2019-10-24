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

package org.jacpfx.vxms.common.throwable;

import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * Created by Andy Moncsek on 21.01.16.
 *
 * A consumer that throws a throwable, so vxms can handle the exceptions
 * @param <T> the return type
 */
@FunctionalInterface
public interface ThrowableFutureConsumer<T> {


  /**
   * Performs this operation on the given argument.
   *
   * @param operationResult the input argument with the return type
   * @throws Throwable the throwable
   */
  void accept(Promise<T> operationResult) throws Throwable;
}
