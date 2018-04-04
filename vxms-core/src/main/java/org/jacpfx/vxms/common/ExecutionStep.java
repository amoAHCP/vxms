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

package org.jacpfx.vxms.common;

import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;

/**
 * Keep the executions steps in supply/andThen
 *
 * @author Andy Moncsek
 * @param <V> the type of the input value
 * @param <T> the type of the return value
 */
public class ExecutionStep<V, T> {

  /** the first step (supply) in a chain */
  private final ThrowableFutureConsumer<T> chainconsumer;

  /** An execution step */
  private final ThrowableFutureBiConsumer<T, V> step;

  public ExecutionStep(ThrowableFutureConsumer<T> chainconsumer) {
    this.chainconsumer = chainconsumer;
    this.step = null;
  }

  public ExecutionStep(ThrowableFutureBiConsumer<T, V> step) {
    this.step = step;
    this.chainconsumer = null;
  }

  /**
   * Returns a first step from the supply method
   *
   * @return the supply execution step
   */
  public ThrowableFutureConsumer<T> getChainconsumer() {
    return chainconsumer;
  }

  /**
   * Returns an execution step (andThen)
   * @return an execution step
   */
  public ThrowableFutureBiConsumer<T, V> getStep() {
    return step;
  }
}
